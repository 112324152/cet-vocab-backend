package com.cetvocab.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.model.DayCountDto;
import com.cetvocab.backend.model.DayWordDto;
import com.cetvocab.backend.security.CurrentUser;
import com.cetvocab.backend.service.WordService;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private static final int DAILY_GOAL = 50;

    private final WordService wordService;

    public ProgressController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/today")
    public Map<String, Object> today() {
        long count = wordService.todayCount(CurrentUser.id());
        return Map.of("count", count, "goal", DAILY_GOAL);
    }

    @GetMapping("/month")
    public List<DayCountDto> month(@RequestParam int year, @RequestParam int month) {
        return wordService.monthCounts(CurrentUser.id(), year, month);
    }

    @GetMapping("/day")
    public List<DayWordDto> day(@RequestParam String date) {
        return wordService.dayWords(CurrentUser.id(), date);
    }
}
