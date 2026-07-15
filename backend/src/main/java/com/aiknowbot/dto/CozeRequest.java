package com.aiknowbot.dto;

import java.util.List;

public class CozeRequest {
    private String botId;
    private String userId;
    private List<CozeMessage> messages;
    private boolean stream;

    public CozeRequest() {}

    public CozeRequest(String botId, String userId, List<CozeMessage> messages, boolean stream) {
        this.botId = botId;
        this.userId = userId;
        this.messages = messages;
        this.stream = stream;
    }

    public String getBotId() { return botId; }
    public void setBotId(String botId) { this.botId = botId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<CozeMessage> getMessages() { return messages; }
    public void setMessages(List<CozeMessage> messages) { this.messages = messages; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }

    public static class CozeMessage {
        private String role;
        private String content;
        private String contentType;

        public CozeMessage() {}

        public CozeMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.contentType = "text";
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
    }
}
