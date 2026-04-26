package com.wyjqwy.server.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.model.entity.CategoryEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VoiceAccountingAiService {
    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public VoiceAccountingAiService(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 解析一次语音/文本中的多条记账。大模型应始终返回形如 {"items":[{...},...]} 的 JSON 对象
     * （单条也必须放在 items 数组里）。仍兼容单对象或裸数组的旧输出。
     */
    public List<ParsedVoiceTransaction> parse(String voiceText, List<CategoryEntity> categories, LocalDateTime fallbackOccurredAt) {
        if (!StringUtils.hasText(resolveApiKey())) {
            throw new BizException("AI API key 未配置");
        }
        String content = requestCompletion(voiceText, categories);
        try {
            JsonNode root = objectMapper.readTree(content);
            if (root.isArray()) {
                return parseArrayElements(root, fallbackOccurredAt, "AI 返回的数组为空或无效");
            }
            if (root.isObject()) {
                if (root.has("items") && root.get("items").isArray()) {
                    return parseArrayElements(root.get("items"), fallbackOccurredAt, "AI 返回的 items 为空或无效");
                }
                if (root.has("type") && root.has("amount") && root.has("categoryId")) {
                    return List.of(parseItem(root, fallbackOccurredAt));
                }
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            // fall through
        }
        throw new BizException("AI 返回格式无法解析：需 { \"items\": [ ... ] } 或等价的 object[]");
    }

    public String transcribeFromAudioBase64(String audioBase64) {
        if (!"zhipu".equals(resolveProvider())) {
            throw new BizException("当前 AI 提供商不支持语音转文字，请切换到智谱或改用文本输入");
        }
        if (!StringUtils.hasText(audioBase64)) {
            throw new BizException("audioBase64 is required");
        }
        if (!StringUtils.hasText(properties.getZhipu().getApiKey())) {
            throw new BizException("智谱 AI API key 未配置");
        }
        byte[] audioBytes;
        try {
            audioBytes = Base64.getDecoder().decode(audioBase64);
        } catch (Exception e) {
            throw new BizException("audioBase64 格式无效");
        }
        if (audioBytes.length == 0) {
            throw new BizException("音频内容为空");
        }

        RestClient client = RestClient.builder().baseUrl(properties.getZhipu().getBaseUrl()).build();

        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return "voice.m4a";
            }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType("audio/mp4"));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(audioResource, partHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", properties.getZhipu().getAsrModel());
        body.add("stream", "false");
        body.add("file", filePart);

        String responseBody = client.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + properties.getZhipu().getApiKey())
                .body(body)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("text").asText("");
            if (!StringUtils.hasText(text)) {
                throw new BizException("语音转文字失败：未返回文本");
            }
            return text.trim();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("语音转文字响应解析失败");
        }
    }

    private List<ParsedVoiceTransaction> parseArrayElements(JsonNode array, LocalDateTime fallbackOccurredAt, String ifEmpty) {
        List<ParsedVoiceTransaction> out = new ArrayList<>();
        for (JsonNode el : array) {
            if (el != null && el.isObject()) {
                out.add(parseItem(el, fallbackOccurredAt));
            }
        }
        if (out.isEmpty()) {
            throw new BizException(ifEmpty);
        }
        return out;
    }

    private ParsedVoiceTransaction parseItem(JsonNode root, LocalDateTime fallbackOccurredAt) {
        int type = root.path("type").asInt(0);
        if (type != 1 && type != 2) {
            throw new BizException("AI 解析失败：type 无效");
        }
        BigDecimal amount = root.path("amount").decimalValue().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("AI 解析失败：amount 无效");
        }
        long categoryId = root.path("categoryId").asLong(0L);
        if (categoryId <= 0) {
            throw new BizException("AI 解析失败：categoryId 无效");
        }
        String note = root.path("note").isMissingNode() || root.path("note").isNull() ? null : root.path("note").asText();
        LocalDateTime occurredAt = fallbackOccurredAt;
        JsonNode occurredAtNode = root.path("occurredAt");
        if (!occurredAtNode.isMissingNode() && !occurredAtNode.isNull() && StringUtils.hasText(occurredAtNode.asText())) {
            String raw = occurredAtNode.asText().trim();
            try {
                occurredAt = LocalDateTime.parse(raw);
            } catch (DateTimeParseException e1) {
                try {
                    // 兼容部分模型输出 "yyyy-MM-dd HH:mm:ss" 的情况
                    occurredAt = LocalDateTime.parse(raw.replace(" ", "T"));
                } catch (DateTimeParseException ignored) {
                    // keep fallbackOccurredAt
                }
            }
        }
        // 时间合理性保护：仅接受“当前时间前后半年”范围内的时间，超出则回退当前时间。实际10个月
        LocalDateTime minAllowed = fallbackOccurredAt.minusMonths(10);
        LocalDateTime maxAllowed = fallbackOccurredAt.plusMonths(10);
        if (occurredAt.isBefore(minAllowed) || occurredAt.isAfter(maxAllowed)) {
            occurredAt = fallbackOccurredAt;
        }
        return new ParsedVoiceTransaction(type, amount, categoryId, note, occurredAt);
    }

    private String requestCompletion(String voiceText, List<CategoryEntity> categories) {
        String systemPrompt = """
                            你是记账助手。请根据用户输入，对照我提供的分类列表，解析出结构化记账明细。
                            【分类匹配原则】—— 重点：
                            1. 互联网服务：所有软件订阅、会员（如：谷歌会员、iCloud、视频会员）、游戏充值归入“休闲娱乐”。
                            2. 电子产品：购买手机、电脑、耳机等硬件归入“数码电器”。
                            3. 餐饮：仅限吃饭、外卖、奶茶、买菜。
                            4. 默认规则：如果语义模糊，优先匹配名称包含该关键词的分类；若完全不相关，归入“其他支出”)。
                            5. 优化规则：如果语义匹配多个分类，选择相关度高的分类，比如分类中餐饮、零食、奶茶、果茶，用户输入了奶茶，优先匹配奶茶分类，其次是果茶、其次是零食最后是餐饮)。
                            【输出要求】
                            只返回一个标准 JSON 对象，不要解释，不要 Markdown 块。
                            根结构：{ "items": [ { ... } ] }
                            字段要求：
                            - type: 1(支出), 2(收入)
                            - amount: 正数
                            - categoryId: 必须从给定的 id 中选择
                            - note: 简短备注（不超6字）
                            - occurredAt: 本地时间，格式必须为 yyyy-MM-ddTHH:mm:ss
                            时间规则：
                            如果用户输入里有明确时间（含可确定的年月日），occurredAt 就用用户输入的时间；
                            如果没有识别出时间，或无法确认具体的年月信息，则 occurredAt 使用当前时间（即“现在”）。
                            即使识别到时间，也必须在“当前时间前后10个月”内；超出10个月范围一律使用当前时间。
                            """;

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("voiceText", voiceText);
        userPayload.put("categories", categories.stream().map(c -> Map.of(
                "id", c.getId(),
                "type", c.getType(),
                "name", c.getName()
        )).toList());

        return switch (resolveProvider()) {
            case "google" -> requestCompletionFromGoogle(systemPrompt, userPayload);
            case "zhipu" -> requestCompletionFromZhipu(systemPrompt, userPayload);
            default -> throw new BizException("不支持的 AI 提供商：" + resolveProvider());
        };
    }

    private String requestCompletionFromZhipu(String systemPrompt, Map<String, Object> userPayload) {
        RestClient client = RestClient.builder().baseUrl(properties.getZhipu().getBaseUrl()).build();
        Map<String, Object> request = new HashMap<>();
        request.put("model", properties.getZhipu().getModel());
        request.put("temperature", 0.1);
        request.put("response_format", Map.of("type", "json_object"));
        request.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", toJson(userPayload))
        ));

        String responseBody = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getZhipu().getApiKey())
                .body(request)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!StringUtils.hasText(content.asText())) {
                throw new BizException("AI 返回为空");
            }
            return content.asText();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("AI 接口响应解析失败");
        }
    }

    private String requestCompletionFromGoogle(String systemPrompt, Map<String, Object> userPayload) {
        RestClient client = RestClient.builder().baseUrl(properties.getGoogle().getBaseUrl()).build();
        String combinedPrompt = systemPrompt + "\n\n用户输入(JSON)：\n" + toJson(userPayload);
        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", combinedPrompt)))
        ));
        request.put("generationConfig", Map.of(
                "temperature", 0.1,
                "responseMimeType", "application/json"
        ));
        String responseBody = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/{model}:generateContent")
                        .queryParam("key", properties.getGoogle().getApiKey())
                        .build(properties.getGoogle().getModel()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new BizException("AI 返回为空");
            }
            StringBuilder sb = new StringBuilder();
            for (JsonNode p : parts) {
                String t = p.path("text").asText();
                if (StringUtils.hasText(t)) sb.append(t);
            }
            if (!StringUtils.hasText(sb.toString())) {
                throw new BizException("AI 返回为空");
            }
            return sb.toString();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("Google AI 接口响应解析失败");
        }
    }

    private String resolveProvider() {
        String p = properties.getProvider();
        return StringUtils.hasText(p) ? p.trim().toLowerCase() : "zhipu";
    }

    private String resolveApiKey() {
        return switch (resolveProvider()) {
            case "google" -> properties.getGoogle().getApiKey();
            case "zhipu" -> properties.getZhipu().getApiKey();
            default -> null;
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BizException("AI 请求构造失败");
        }
    }

    public record ParsedVoiceTransaction(
            Integer type,
            BigDecimal amount,
            Long categoryId,
            String note,
            LocalDateTime occurredAt
    ) {
    }
}
