package com.aiknowbot.dto;

import java.util.List;

public class CozeResponse {
    private String code;
    private String msg;
    private Data data;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    public boolean isSuccess() {
        return "0".equals(code);
    }

    public static class Data {
        private String conversationId;
        private List<MessageData> messages;

        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public List<MessageData> getMessages() { return messages; }
        public void setMessages(List<MessageData> messages) { this.messages = messages; }
    }

    public static class MessageData {
        private String role;
        private String content;
        private String contentType;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
    }
}
