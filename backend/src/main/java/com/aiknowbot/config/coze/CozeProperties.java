package com.aiknowbot.config.coze;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "coze.api")
public class CozeProperties {
    private String baseUrl;
    private String token;
    private String botId;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getBotId() { return botId; }
    public void setBotId(String botId) { this.botId = botId; }
}
