package com.wyjqwy.server.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai.zhipu")
public class ZhipuAiProperties {
    private String apiKey;
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
    private String model = "glm-4.7-flash";
    private String asrModel = "glm-asr-2512";
}
