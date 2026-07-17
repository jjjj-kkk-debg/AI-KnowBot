package com.aiknowbot.service;

import com.aiknowbot.config.coze.CozeProperties;
import com.aiknowbot.dto.CozeRequest;
import com.aiknowbot.dto.CozeRequest.CozeMessage;
import com.aiknowbot.dto.CozeResponse;
import com.aiknowbot.dto.CozeResponse.ChatData;
import com.aiknowbot.dto.CozeResponse.MessageData;
import com.aiknowbot.dto.CozeResponse.MessageListResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CozeService {

    private static final Logger log = LoggerFactory.getLogger(CozeService.class);
    private static final int MAX_RETRIES = 900;
    private static final long RETRY_INTERVAL_MS = 200;
    private static final long MAX_POLL_TIME_MS = 180_000;

    private final CozeProperties cozeProperties;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final AsyncTaskManager taskManager;
    private ExecutorService executor;

    public CozeService(CozeProperties cozeProperties, AsyncTaskManager taskManager) {
        this.cozeProperties = cozeProperties;
        this.taskManager = taskManager;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @PostConstruct
    public void init() {
        this.executor = Executors.newCachedThreadPool();
    }

    public String chat(String userId, String userMessage) {
        CozeMessage message = new CozeMessage("user", userMessage);
        CozeRequest request = new CozeRequest(
                cozeProperties.getBotId(),
                userId,
                Collections.singletonList(message),
                false
        );

        String jsonBody = gson.toJson(request);
        log.debug("Coze request: {}", jsonBody);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request httpRequest = new Request.Builder()
                .url(cozeProperties.getBaseUrl() + "/v3/chat")
                .addHeader("Authorization", "Bearer " + cozeProperties.getToken())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("Coze create chat response: {}", responseBody);

            if (!response.isSuccessful()) {
                log.error("Coze API error: {} {}", response.code(), responseBody);
                return "抱歉，AI服务暂时不可用，请稍后重试。";
            }

            ChatData chatData = extractChatData(responseBody);
            if (chatData == null) {
                return "抱歉，AI服务暂时不可用，请稍后重试。";
            }

            String chatId = chatData.getId();
            String conversationId = chatData.getConversationId();

            return pollForResult(conversationId, chatId);
        } catch (IOException e) {
            log.error("Coze API call failed", e);
            return "抱歉，网络连接失败，请检查网络后重试。";
        }
    }

    public long chatAsync(String userId, String userMessage) {
        long taskId = taskManager.createTask();

        executor.submit(() -> {
            try {
                String result = chat(userId, userMessage);
                taskManager.completeTask(taskId, result);
            } catch (Exception e) {
                log.error("Async chat failed", e);
                taskManager.failTask(taskId, "AI处理异常，请重试。");
            }
        });

        return taskId;
    }

    private ChatData extractChatData(String json) {
        try {
            CozeResponse wrapper = gson.fromJson(json, CozeResponse.class);
            if (wrapper == null || !wrapper.isSuccess()) {
                log.error("Coze API error response: {}", json);
                return null;
            }
            String dataJson = gson.toJson(wrapper.getData());
            return gson.fromJson(dataJson, ChatData.class);
        } catch (Exception e) {
            log.error("Failed to parse chat data", e);
            return null;
        }
    }

    private String pollForResult(String conversationId, String chatId) {
        long startTime = System.currentTimeMillis();
        long lastLog = startTime;
        for (int i = 0; i < MAX_RETRIES; i++) {
            long now = System.currentTimeMillis();
            if (now - lastLog > 10_000) {
                log.info("Polling chat: conv={} chat={} elapsed={}s", conversationId, chatId, (now - startTime) / 1000);
                lastLog = now;
            }
            if (System.currentTimeMillis() - startTime > MAX_POLL_TIME_MS) {
                return "AI处理超时，请重试。";
            }

            String status = getChatStatus(conversationId, chatId);
            log.debug("Polling chat status: {} (attempt {})", status, i + 1);

            if ("completed".equals(status)) {
                return getChatMessages(conversationId, chatId);
            } else if ("failed".equals(status)) {
                return "AI处理失败，请重试。";
            }

            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "请求被中断，请重试。";
            }
        }
        return "AI处理超时，请重试。";
    }

    private String getChatStatus(String conversationId, String chatId) {
        String encodedConvId = URLEncoder.encode(conversationId, StandardCharsets.UTF_8);
        String encodedChatId = URLEncoder.encode(chatId, StandardCharsets.UTF_8);
        String url = cozeProperties.getBaseUrl() + "/v3/chat/retrieve?conversation_id=" + encodedConvId + "&chat_id=" + encodedChatId;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + cozeProperties.getToken())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            log.debug("Chat status response: {}", body);
            ChatData chatData = extractChatData(body);
            return chatData != null ? chatData.getStatus() : "unknown";
        } catch (IOException e) {
            log.error("Failed to get chat status", e);
            return "unknown";
        }
    }

    private String getChatMessages(String conversationId, String chatId) {
        String encodedConvId = URLEncoder.encode(conversationId, StandardCharsets.UTF_8);
        String encodedChatId = URLEncoder.encode(chatId, StandardCharsets.UTF_8);
        String url = cozeProperties.getBaseUrl() + "/v3/chat/message/list?conversation_id=" + encodedConvId + "&chat_id=" + encodedChatId;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + cozeProperties.getToken())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            log.debug("Chat messages response: {}", body);

            CozeResponse wrapper = gson.fromJson(body, CozeResponse.class);
            if (wrapper == null || !wrapper.isSuccess()) {
                return "抱歉，获取AI回复失败。";
            }

            String dataJson = gson.toJson(wrapper.getData());
            Type listType = new TypeToken<List<MessageData>>(){}.getType();
            List<MessageData> messages = gson.fromJson(dataJson, listType);

            if (messages != null && !messages.isEmpty()) {
                StringBuilder result = new StringBuilder();
                for (MessageData msg : messages) {
                    if ("assistant".equals(msg.getRole()) && "answer".equals(msg.getType())) {
                        String content = msg.getContent();
                        String imgUrl = extractImageUrl(content);
                        if (imgUrl != null) {
                            if (result.length() > 0) result.append("<br><br>");
                            result.append("<img src=\"").append(imgUrl).append("\" style=\"max-width:100%;border-radius:8px;\" alt=\"生成的图片\"><br>");
                            String desc = extractImageDescription(content);
                            if (desc != null) result.append(desc);
                        } else {
                            if (result.length() > 0) result.append("<br><br>");
                            result.append(content.replace("\n", "<br>"));
                        }
                    }
                }
                return result.length() > 0 ? result.toString() : "AI没有返回有效内容。";
            }
            return "AI没有返回有效内容。";
        } catch (IOException e) {
            log.error("Failed to get chat messages", e);
            return "抱歉，获取AI回复失败。";
        }
    }

    public String generateImage(String userId, String prompt) {
        String enhancedPrompt = "请根据以下描述生成一张图片，只返回图片的URL和简短说明：\n" + prompt;
        return chat(userId, enhancedPrompt);
    }

    public String generateArticle(String userId, String topic, String keywords) {
        String articlePrompt = String.format(
                "请撰写一篇关于'%s'的文章，包含以下关键词：%s。要求：结构完整，有标题、引言、正文和结论。",
                topic, keywords
        );
        return chat(userId, articlePrompt);
    }

    public String analyzeData(String userId, String data, String analysisType) {
        String analysisPrompt = String.format(
                "请对以下数据进行%s分析，给出详细的分析结果和建议：\n%s",
                analysisType, data
        );
        return chat(userId, analysisPrompt);
    }

    private String extractImageUrl(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("https://s\\.coze\\.cn/t/[a-zA-Z0-9_/+-]+");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractImageDescription(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("http") && !trimmed.startsWith("{")) {
                return trimmed;
            }
        }
        return null;
    }
}
