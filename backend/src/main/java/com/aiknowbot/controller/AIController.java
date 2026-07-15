package com.aiknowbot.controller;

import com.aiknowbot.dto.ApiResult;
import com.aiknowbot.service.CozeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final CozeService cozeService;

    public AIController(CozeService cozeService) {
        this.cozeService = cozeService;
    }

    @PostMapping("/ask")
    public ApiResult<String> ask(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ApiResult.error("问题不能为空");
        }
        return ApiResult.success(cozeService.chat(userId, question));
    }

    @PostMapping("/generate-image")
    public ApiResult<String> generateImage(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String prompt = body.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ApiResult.error("图片描述不能为空");
        }
        return ApiResult.success(cozeService.generateImage(userId, prompt));
    }

    @PostMapping("/generate-article")
    public ApiResult<String> generateArticle(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String topic = body.get("topic");
        String keywords = body.getOrDefault("keywords", "");
        if (topic == null || topic.trim().isEmpty()) {
            return ApiResult.error("文章主题不能为空");
        }
        return ApiResult.success(cozeService.generateArticle(userId, topic, keywords));
    }

    @PostMapping("/analyze")
    public ApiResult<String> analyze(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String data = body.get("data");
        String type = body.getOrDefault("type", "通用");
        if (data == null || data.trim().isEmpty()) {
            return ApiResult.error("分析数据不能为空");
        }
        return ApiResult.success(cozeService.analyzeData(userId, data, type));
    }
}
