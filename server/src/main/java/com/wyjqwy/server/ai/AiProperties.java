package com.wyjqwy.server.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    /**
     * 可选：bailian（阿里云百炼/DashScope）/ zhipu / google
     * 亦可用别名：dashscope、aliyun
     */
    private String provider = "bailian";

    private Bailian bailian = new Bailian();
    private Zhipu zhipu = new Zhipu();
    private Google google = new Google();

    @Data
    public static class Bailian {
        /** 百炼控制台 API Key：https://bailian.console.aliyun.com/ */
        private String apiKey;
        /** OpenAI 兼容模式根地址，末尾不要带 /chat/completions */
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        /** 模型名，仅在此或 application.yml 中修改，勿写死在业务代码里 */
        private String model = "qwen-plus-2025-07-28";
        private double temperature = 0.1;
    }

    @Data
    public static class Zhipu {
        private String apiKey;
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        private String model = "glm-4.7-flash";
        private String asrModel = "glm-asr-2512";
    }

    @Data
    public static class Google {
        private String apiKey;
        private String baseUrl = "https://generativelanguage.googleapis.com";
        private String model = "gemini-2.0-flash";
    }
}
