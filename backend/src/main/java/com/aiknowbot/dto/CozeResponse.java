package com.aiknowbot.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CozeResponse {
    private String code;
    private String msg;
    private Object data;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public boolean isSuccess() {
        return "0".equals(code);
    }

    public static class ChatData {
        private String id;
        @SerializedName("conversation_id")
        private String conversationId;
        @SerializedName("bot_id")
        private String botId;
        private String status;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public String getBotId() { return botId; }
        public void setBotId(String botId) { this.botId = botId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class MessageData {
        private String role;
        private String content;
        private String type;
        @SerializedName("content_type")
        private String contentType;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
    }

    public static class MessageListResult {
        private List<MessageData> messages;

        public List<MessageData> getMessages() { return messages; }
        public void setMessages(List<MessageData> messages) { this.messages = messages; }
    }
}
