package com.example.demo.upload;

import com.example.demo.model.Job;
import com.example.demo.model.JobRepository;
import com.example.demo.parsing.PdfExtractor;
import com.example.demo.queue.ChunkPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final PdfExtractor pdfExtractor;
    private final ChunkPublisher chunkPublisher;
    private final JobRepository jobRepository;

    public UploadController(PdfExtractor pdfExtractor, ChunkPublisher chunkPublisher, JobRepository jobRepository) {
        this.pdfExtractor = pdfExtractor;
        this.chunkPublisher = chunkPublisher;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, String> chunks = pdfExtractor.extract(file);

        String jobId = java.util.UUID.randomUUID().toString();
        Job job = new Job(jobId, file.getOriginalFilename(), chunks.size());
        jobRepository.save(job);

        chunkPublisher.publish(chunks, jobId, file.getOriginalFilename());

        return Map.of(
            "job_id", jobId,
            "filename", file.getOriginalFilename(),
            "total_chunks", chunks.size(),
            "status", "processing"
        );
    }
}