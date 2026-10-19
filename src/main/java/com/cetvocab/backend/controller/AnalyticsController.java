package com.cetvocab.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.model.AnalyticsOverviewDto;
import com.cetvocab.backend.model.DailyStatDto;
import com.cetvocab.backend.model.HardestWordDto;
import com.cetvocab.backend.model.IntervalBucketDto;
import com.cetvocab.backend.service.WordService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final WordService wordService;

    public AnalyticsController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewDto overview() {
        return wordService.analyticsOverview();
    }

    @GetMapping("/daily")
    public List<DailyStatDto> daily(@RequestParam(defaultValue = "7") int days) {
        return wordService.dailyStats(Math.min(Math.max(days, 1), 90));
    }

    @GetMapping("/hardest")
    public List<HardestWordDto> hardest(@RequestParam(defaultValue = "10") int limit) {
        return wordService.hardestWords(Math.min(Math.max(limit, 1), 50));
    }

    @GetMapping("/intervals")
    public List<IntervalBucketDto> intervals() {
        return wordService.intervalDistribution();
    }
}