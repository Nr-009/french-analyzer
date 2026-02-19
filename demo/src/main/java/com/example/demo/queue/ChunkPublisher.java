package com.example.demo.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ChunkPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;  

    public ChunkPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;  
    }

    public void publish(Map<String, String> chunks, String jobId, String filename) throws Exception {
        int index = 0;
        for (Map.Entry<String, String> entry : chunks.entrySet()) {
            Map<String, Object> message = Map.of(
                "job_id", jobId,
                "filename", filename,
                "chunk_index", index,
                "chunk_name", entry.getKey(),
                "text", entry.getValue()
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHUNK_QUEUE,
                objectMapper.writeValueAsString(message)
            );
            index++;
        }
    }
}