package com.cetvocab.backend.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DictLookupClient {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public String lookupTranslation(String word) {
        try {
            String url = "https://dict.youdao.com/suggest?num=1&ver=3.0&doctype=json&cache=false&le=en&q="
                    + URLEncoder.encode(word, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return "";
            JsonNode root = mapper.readTree(res.body());
            JsonNode entries = root.path("data").path("entries");
            if (entries.isArray() && entries.size() > 0) {
                return entries.get(0).path("explain").asText("");
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
