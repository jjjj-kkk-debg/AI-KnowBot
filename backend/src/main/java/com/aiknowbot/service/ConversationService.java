package com.aiknowbot.service;

import com.aiknowbot.entity.Conversation;
import com.aiknowbot.entity.Message;
import com.aiknowbot.repository.ConversationRepository;
import com.aiknowbot.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final CozeService cozeService;

    public ConversationService(ConversationRepository conversationRepository,
                                MessageRepository messageRepository,
                                CozeService cozeService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.cozeService = cozeService;
    }

    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Conversation createConversation(String userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        return conversationRepository.save(conversation);
    }

    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public AnswerResult sendMessage(Long conversationId, String userId, String content) {
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setMessageType("text");
        messageRepository.save(userMessage);

        String aiResponse = cozeService.chat(userId, content);

        Message aiMessage = new Message();
        aiMessage.setConversationId(conversationId);
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        aiMessage.setMessageType("text");
        messageRepository.save(aiMessage);

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation != null) {
            String title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }

        return new AnswerResult(aiResponse, conversationId);
    }

    public static class AnswerResult {
        private String content;
        private Long conversationId;

        public AnswerResult(String content, Long conversationId) {
            this.content = content;
            this.conversationId = conversationId;
        }

        public String getContent() { return content; }
        public Long getConversationId() { return conversationId; }
    }
}
