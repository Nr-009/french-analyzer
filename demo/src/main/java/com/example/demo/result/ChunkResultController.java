package com.example.demo.result;

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

    public ChunkResultController(JobRepository jobRepository,
                                  ChunkResultRepository chunkResultRepository,
                                  RedisTemplate<String, String> redisTemplate) {
        this.jobRepository = jobRepository;
        this.chunkResultRepository = chunkResultRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/chunk-result")
    @Transactional
    public synchronized Map<String, String> receiveResult(@RequestBody Map<String, Object> result) {
        String jobId = (String) result.get("job_id");
        int chunkIndex = (int) result.get("chunk_index");
        String chunkName = (String) result.get("chunk_name");
        double difficultyScore = (double) result.get("difficulty_score");

        Job job = jobRepository.findById(jobId).orElseThrow();
        ChunkResult chunkResult = new ChunkResult(job, chunkIndex, chunkName, difficultyScore);
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