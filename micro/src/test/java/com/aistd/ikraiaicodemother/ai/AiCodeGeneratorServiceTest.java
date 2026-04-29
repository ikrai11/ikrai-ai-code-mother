package com.aistd.ikraiaicodemother.ai;

import com.aistd.ikraiaicodemother.ai.model.HtmlCodeResult;
import com.aistd.ikraiaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult code = aiCodeGeneratorService.generateHtmlCode("做一个程序员ikrai的博客，不超过20行");
        Assertions.assertNotNull(code);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult code = aiCodeGeneratorService.generateMultiFileCode("做一个程序员ikrai的留言板，不超过30行");
        Assertions.assertNotNull(code);
    }
}