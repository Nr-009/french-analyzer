package com.example.demo.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ChunkPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ChunkPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Map<String, String> chunks, String jobId, 
                        String filename, boolean hasAnki, Set<String> knownWords) throws Exception {
        int index = 0;
        for (Map.Entry<String, String> entry : chunks.entrySet()) {
            Map<String, Object> message = new HashMap<>();
            message.put("job_id", jobId);
            message.put("filename", filename);
            message.put("chunk_index", index);
            message.put("chunk_name", entry.getKey());
            message.put("text", entry.getValue());
            message.put("has_anki", hasAnki);
            message.put("known_words", knownWords);

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHUNK_QUEUE,
                objectMapper.writeValueAsString(message)
            );
            index++;
        }
    }
}