package com.cetvocab.backend.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LlmClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    /**
     * 调用 OpenAI 兼容的对话接口，流式读取。
     * 这是阻塞方法：每读到一块就回调 onDelta，直到模型输出结束。
     * 调用方必须把它放在独立线程里执行。
     */
    public void streamChat(List<Map<String, String>> messages, Consumer<String> onDelta) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", messages,
                    "stream", true
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                response.body().close();
                throw new RuntimeException("模型服务返回状态码 " + response.statusCode());
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(data)) {
                        return;
                    }

                    JsonNode node = objectMapper.readTree(data);
                    JsonNode choices = node.path("choices");
                    if (choices.isArray() && !choices.isEmpty()) {
                        String delta = choices.get(0).path("delta").path("content").asText("");
                        if (!delta.isEmpty()) {
                            onDelta.accept(delta);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("调用模型失败：" + e.getMessage(), e);
        }
    }
}