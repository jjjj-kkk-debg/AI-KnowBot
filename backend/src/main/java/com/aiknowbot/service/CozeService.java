package com.aiknowbot.service;

import com.aiknowbot.config.coze.CozeProperties;
import com.aiknowbot.dto.CozeRequest;
import com.aiknowbot.dto.CozeRequest.CozeMessage;
import com.aiknowbot.dto.CozeResponse;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class CozeService {

    private static final Logger log = LoggerFactory.getLogger(CozeService.class);

    private final CozeProperties cozeProperties;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public CozeService(CozeProperties cozeProperties) {
        this.cozeProperties = cozeProperties;
        this.gson = new Gson();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
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
            log.debug("Coze response: {}", responseBody);

            CozeResponse cozeResponse = gson.fromJson(responseBody, CozeResponse.class);
            if (cozeResponse != null && cozeResponse.isSuccess()
                    && cozeResponse.getData() != null
                    && cozeResponse.getData().getMessages() != null
                    && !cozeResponse.getData().getMessages().isEmpty()) {
                return cozeResponse.getData().getMessages().get(0).getContent();
            }
            return "抱歉，AI服务暂时不可用，请稍后重试。";
        } catch (IOException e) {
            log.error("Coze API call failed", e);
            return "抱歉，网络连接失败，请检查网络后重试。";
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
}
