package com.aiknowbot.controller;

import com.aiknowbot.dto.ApiResult;
import com.aiknowbot.service.AsyncTaskManager;
import com.aiknowbot.service.AsyncTaskManager.TaskResult;
import com.aiknowbot.service.AsyncTaskManager.TaskStatus;
import com.aiknowbot.service.CozeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final CozeService cozeService;
    private final AsyncTaskManager taskManager;

    public AIController(CozeService cozeService, AsyncTaskManager taskManager) {
        this.cozeService = cozeService;
        this.taskManager = taskManager;
    }

    @PostMapping("/ask")
    public ApiResult<Long> ask(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ApiResult.error("问题不能为空");
        }
        return ApiResult.success(cozeService.chatAsync(userId, question));
    }

    @PostMapping("/generate-image")
    public ApiResult<Long> generateImage(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String prompt = body.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ApiResult.error("图片描述不能为空");
        }
        String enhancedPrompt = "请根据以下描述生成一张图片，只返回图片的URL和简短说明：\n" + prompt;
        return ApiResult.success(cozeService.chatAsync(userId, enhancedPrompt));
    }

    @PostMapping("/generate-article")
    public ApiResult<Long> generateArticle(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String topic = body.get("topic");
        String keywords = body.getOrDefault("keywords", "");
        if (topic == null || topic.trim().isEmpty()) {
            return ApiResult.error("文章主题不能为空");
        }
        String articlePrompt = String.format(
                "请撰写一篇关于'%s'的文章，包含以下关键词：%s。要求：结构完整，有标题、引言、正文和结论。",
                topic, keywords
        );
        return ApiResult.success(cozeService.chatAsync(userId, articlePrompt));
    }

    @PostMapping("/analyze")
    public ApiResult<Long> analyze(@RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", "anonymous");
        String data = body.get("data");
        String type = body.getOrDefault("type", "通用");
        if (data == null || data.trim().isEmpty()) {
            return ApiResult.error("分析数据不能为空");
        }
        String analysisPrompt = String.format(
                "请对以下数据进行%s分析，给出详细的分析结果和建议：\n%s",
                type, data
        );
        return ApiResult.success(cozeService.chatAsync(userId, analysisPrompt));
    }

    @GetMapping("/result/{taskId}")
    public ApiResult<TaskResult> getResult(@PathVariable long taskId) {
        TaskResult result = taskManager.getTask(taskId);
        if (result == null) {
            return ApiResult.error("任务不存在");
        }
        if (result.getStatus() == TaskStatus.PENDING) {
            return ApiResult.success(result);
        }
        return ApiResult.success(result);
    }
}
