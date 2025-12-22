package com.aistd.ikraiaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class ReasoningStreamingChatModelConfig {
    private String baseUrl;
    private String apiKey;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        //测试环境使用deepseek-chat模型
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
//        //生产环境使用deepseek-reasoner模型
//        final String modelName = "deepseek-reasoner";
//        final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .maxTokens(maxTokens)
                .strictJsonSchema(true)
                .build();
    }
}
