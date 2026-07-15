package com.aiknowbot.controller;

import com.aiknowbot.dto.ApiResult;
import com.aiknowbot.entity.Conversation;
import com.aiknowbot.entity.Message;
import com.aiknowbot.service.ConversationService;
import com.aiknowbot.service.CozeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ConversationService conversationService;
    private final CozeService cozeService;

    public ChatController(ConversationService conversationService, CozeService cozeService) {
        this.conversationService = conversationService;
        this.cozeService = cozeService;
    }

    @GetMapping("/conversations")
    public ApiResult<List<Conversation>> getConversations(@RequestParam(defaultValue = "anonymous") String userId) {
        return ApiResult.success(conversationService.getUserConversations(userId));
    }

    @PostMapping("/conversations")
    public ApiResult<Conversation> createConversation(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String title = body.get("title");
        return ApiResult.success(conversationService.createConversation(userId, title));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResult<List<Message>> getMessages(@PathVariable Long id) {
        return ApiResult.success(conversationService.getConversationMessages(id));
    }

    @PostMapping("/send")
    public ApiResult<ConversationService.AnswerResult> sendMessage(@RequestBody Map<String, Object> body) {
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String userId = (String) body.getOrDefault("userId", "anonymous");
        String content = (String) body.get("content");
        return ApiResult.success(conversationService.sendMessage(conversationId, userId, content));
    }
}
