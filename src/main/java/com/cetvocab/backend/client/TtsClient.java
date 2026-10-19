package com.cetvocab.backend.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

@Component
public class TtsClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 从有道词典取单词发音（mp3 字节流）。
     * 有道这个接口长期稳定、免费，但属于非官方接口。
     * 如果未来失效，只需替换本方法内部实现，调用方无需改动。
     */
    public byte[] fetchAudio(String word) {
        try {
            String encoded = URLEncoder.encode(word, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String url = "https://dict.youdao.com/dictvoice?audio="
                    + encoded + "&type=2";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200 || response.body().length == 0) {
                throw new RuntimeException("TTS 服务返回异常，状态码 " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("获取发音失败：" + e.getMessage(), e);
        }
    }
}