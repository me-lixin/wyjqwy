package com.wyjqwy.server.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    /**
     * 可选：zhipu / google
     */
    private String provider = "zhipu";

    private Zhipu zhipu = new Zhipu();
    private Google google = new Google();

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
