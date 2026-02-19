package com.example.demo.result;
import org.springframework.transaction.annotation.Transactional; 
import com.example.demo.model.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChunkResultController {

    private final JobRepository jobRepository;
    private final ChunkResultRepository chunkResultRepository;

    public ChunkResultController(JobRepository jobRepository, ChunkResultRepository chunkResultRepository) {
        this.jobRepository = jobRepository;
        this.chunkResultRepository = chunkResultRepository;
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

    if (newCount >= job.getTotalChunks()) {
    jobRepository.updateStatus(jobId, "COMPLETE");
    }
    return Map.of("status", "saved");
    }
}