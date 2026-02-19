package com.example.demo.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CHUNK_QUEUE = "chunk.queue";

    @Bean
    public Queue chunkQueue() {
        return new Queue(CHUNK_QUEUE, false); 
    }

     @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();  
    }
}