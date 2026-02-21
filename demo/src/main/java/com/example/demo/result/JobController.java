package com.example.demo.job;

import com.example.demo.model.ChunkResult;
import com.example.demo.model.ChunkResultRepository;
import com.example.demo.model.Job;
import com.example.demo.model.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobRepository jobRepository;
    private final ChunkResultRepository chunkResultRepository;

    public JobController(JobRepository jobRepository, ChunkResultRepository chunkResultRepository) {
        this.jobRepository = jobRepository;
        this.chunkResultRepository = chunkResultRepository;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Map<String, Object>> getJob(@PathVariable String jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "jobId", job.getJobId(),
            "filename", job.getFilename(),
            "status", job.getStatus(),
            "totalChunks", job.getTotalChunks(),
            "completedChunks", job.getCompletedChunks()
        ));
    }

 
    @GetMapping("/{jobId}/chunks")
    public ResponseEntity<List<Map<String, Object>>> getChunks(@PathVariable String jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        List<ChunkResult> chunks = chunkResultRepository.findByJob_JobIdOrderByChunkIndexAsc(jobId);

       List<Map<String, Object>> result = chunks.stream().map(chunk -> {
        Map<String, Object> map = new HashMap<>();
        map.put("chunkId", chunk.getChunkId());
        map.put("chunkIndex", chunk.getChunkIndex());
        map.put("chunkName", chunk.getChunkName());
        map.put("difficultyScore", chunk.getDifficultyScore());
        map.put("processingTimeMs", chunk.getProcessingTimeMs());
        map.put("topWords", chunk.getTopWords() != null ? chunk.getTopWords() : "[]");
        map.put("statistics", chunk.getStatistics() != null ? chunk.getStatistics() : "{}");
        return map;}).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}