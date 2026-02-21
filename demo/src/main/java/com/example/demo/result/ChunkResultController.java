package com.example.demo.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.model.*;
import com.example.demo.config.RedisConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChunkResultController {

    private final JobRepository jobRepository;
    private final ChunkResultRepository chunkResultRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChunkResultController(JobRepository jobRepository,
                                  ChunkResultRepository chunkResultRepository,
                                  RedisTemplate<String, String> redisTemplate,
                                  ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.chunkResultRepository = chunkResultRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chunk-result")
    @Transactional
    public synchronized Map<String, String> receiveResult(@RequestBody Map<String, Object> result) {
        String jobId = (String) result.get("job_id");
        int chunkIndex = (int) result.get("chunk_index");
        String chunkName = (String) result.get("chunk_name");
        double difficultyScore = (double) result.get("difficulty_score");
        int processingTimeMs = (int) result.get("processing_time_ms");

        String topWordsJson = null;
        String statisticsJson = null;
        try {
            Object topWords = result.get("top_words");
            Object statistics = result.get("statistics");
            if (topWords != null) topWordsJson = objectMapper.writeValueAsString(topWords);
            if (statistics != null) statisticsJson = objectMapper.writeValueAsString(statistics);
        } catch (Exception e) {
            System.err.println("Failed to serialize top_words or statistics: " + e.getMessage());
        }

        Job job = jobRepository.findById(jobId).orElseThrow();
        ChunkResult chunkResult = new ChunkResult(
            job, chunkIndex, chunkName, difficultyScore,
            processingTimeMs, topWordsJson, statisticsJson
        );
        chunkResultRepository.save(chunkResult);

        jobRepository.incrementCompletedChunks(jobId);
        int newCount = job.getCompletedChunks() + 1;
        System.out.println("Job " + jobId + " progress: " + newCount + "/" + job.getTotalChunks());

        String status = newCount >= job.getTotalChunks() ? "COMPLETE" : "PROCESSING";
        redisTemplate.convertAndSend(RedisConfig.CHUNK_CHANNEL,
            jobId + ":" + newCount + ":" + job.getTotalChunks() + ":" + status);

        if (newCount >= job.getTotalChunks()) {
            jobRepository.updateStatus(jobId, "COMPLETE");
        }

        return Map.of("status", "saved");
    }
}