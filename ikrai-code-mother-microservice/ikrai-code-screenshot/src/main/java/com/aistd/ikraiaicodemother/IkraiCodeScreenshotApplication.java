package com.aistd.ikraiaicodemother;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class IkraiCodeScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(IkraiCodeScreenshotApplication.class, args);
    }
}
