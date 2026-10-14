package com.cetvocab.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cetvocab.backend.mapper")
public class CetVocabBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CetVocabBackendApplication.class, args);
    }
}