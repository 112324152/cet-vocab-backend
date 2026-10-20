package com.cetvocab.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cetvocab.backend.model.BatchLookupRequest;
import com.cetvocab.backend.model.CollectRequest;
import com.cetvocab.backend.model.CollectedWordRow;
import com.cetvocab.backend.model.LookupDto;
import com.cetvocab.backend.service.DictService;

@RestController
@RequestMapping("/api")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/lookup")
    public LookupDto lookup(@RequestParam String word) {
        return dictService.lookup(word);
    }

    @PostMapping("/lookup/batch")
    public List<LookupDto> batchLookup(@RequestBody BatchLookupRequest request) {
        return dictService.batchLookup(request.words());
    }

    @PostMapping("/collect")
    public void collect(@RequestBody CollectRequest request) {
        dictService.collect(request);
    }

    @GetMapping("/collected")
    public List<CollectedWordRow> collected() {
        return dictService.listCollected();
    }
}