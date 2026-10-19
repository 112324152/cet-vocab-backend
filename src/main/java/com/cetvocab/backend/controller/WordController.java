package com.cetvocab.backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.client.TtsClient;
import com.cetvocab.backend.model.MarkRequest;
import com.cetvocab.backend.model.StatsDto;
import com.cetvocab.backend.model.Word;
import com.cetvocab.backend.service.WordService;

@RestController
@RequestMapping("/api")
public class WordController {

    private final WordService wordService;
    private final TtsClient ttsClient;

    public WordController(WordService wordService, TtsClient ttsClient) {
        this.wordService = wordService;
        this.ttsClient = ttsClient;
    }

    @GetMapping("/words")
    public List<Word> words() {
        return wordService.getWords();
    }

    @GetMapping("/words/next")
    public Word next(@RequestParam(required = false) Long excludeId) {
        return wordService.next(excludeId);
    }

    @PostMapping("/words/{id}/state")
    public void markState(@PathVariable Long id, @RequestBody MarkRequest request) {
        wordService.markState(id, request.state());
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        return wordService.stats();
    }

    @GetMapping(value = "/words/{id}/audio", produces = "audio/mpeg")
    public ResponseEntity<byte[]> audio(@PathVariable Long id) {
        try {
            String text = wordService.getWordText(id);
            byte[] audio = ttsClient.fetchAudio(text);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .header("Cache-Control", "public, max-age=86400")
                    .body(audio);
        } catch (Exception e) {
            return ResponseEntity.status(503).build();
        }
    }
}