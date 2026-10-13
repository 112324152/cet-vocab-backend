package com.cetvocab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cetvocab.backend.model.Word;

@Service
public class WordService {

    // 过渡版：词库先写死在内存里，第 4 步会换成从 MySQL 查询。
    public List<Word> getWords() {
        return List.of(
                new Word(1L, "abandon", "/əˈbændən/",
                        List.of("v. 放弃；抛弃", "v. 中止"),
                        "He abandoned his car in the snow.", "CET4"),
                new Word(2L, "benefit", "/ˈbenɪfɪt/",
                        List.of("n. 益处；好处", "v. 有益于"),
                        "Regular exercise benefits your health.", "CET4"),
                new Word(3L, "comprehensive", "/ˌkɒmprɪˈhensɪv/",
                        List.of("adj. 综合的；全面的"),
                        "The report gives a comprehensive analysis of the market.", "CET4"),
                new Word(4L, "dilemma", "/dɪˈlemə/",
                        List.of("n. 困境；进退两难"),
                        "She faced a difficult dilemma.", "CET6"),
                new Word(5L, "essential", "/ɪˈsenʃl/",
                        List.of("adj. 必不可少的；本质的", "n. 要素"),
                        "Water is essential to life.", "CET4"),
                new Word(6L, "fascinate", "/ˈfæsɪneɪt/",
                        List.of("v. 使着迷；深深吸引"),
                        "The idea of space travel fascinates me.", "CET4"),
                new Word(7L, "genuine", "/ˈdʒenjuɪn/",
                        List.of("adj. 真正的；真诚的"),
                        "She showed genuine concern for others.", "CET6"),
                new Word(8L, "hypothesis", "/haɪˈpɒθəsɪs/",
                        List.of("n. 假设；假说"),
                        "The results support our hypothesis.", "CET6"),
                new Word(9L, "inevitable", "/ɪnˈevɪtəbl/",
                        List.of("adj. 不可避免的；必然的"),
                        "Change is inevitable.", "CET4"),
                new Word(10L, "justify", "/ˈdʒʌstɪfaɪ/",
                        List.of("v. 证明…正当；为…辩护"),
                        "Nothing can justify such behavior.", "CET4"),
                new Word(11L, "legitimate", "/lɪˈdʒɪtɪmət/",
                        List.of("adj. 合法的；合理的"),
                        "That is a legitimate question.", "CET6"),
                new Word(12L, "magnificent", "/mæɡˈnɪfɪsnt/",
                        List.of("adj. 壮丽的；宏伟的"),
                        "The view from the top of the mountain was magnificent.", "CET4")
        );
    }
}