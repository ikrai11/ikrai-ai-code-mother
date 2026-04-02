package com.aistd.ikraiaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.aistd.ikraiaicodemother.mapper")
@ComponentScan("com.aistd")
public class IkraiAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(IkraiAiCodeUserApplication.class, args);
    }
}
