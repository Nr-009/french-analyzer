package com.example.demo.upload;

import com.example.demo.model.Job;
import com.example.demo.model.JobRepository;
import com.example.demo.parsing.PdfExtractor;
import com.example.demo.queue.ChunkPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "hasAnki", defaultValue = "false") boolean hasAnki,
            @RequestParam(value = "ankiFile", required = false) MultipartFile ankiFile
    ) throws Exception {

        Set<String> knownWords = new HashSet<>();
        if (hasAnki && ankiFile != null && !ankiFile.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ankiFile.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String word = line.split("\t")[0].trim().toLowerCase();
                    if (!word.isEmpty()) knownWords.add(word);
                }
            }
        }

        Map<String, String> chunks = pdfExtractor.extract(file);
        String jobId = java.util.UUID.randomUUID().toString();
        Job job = new Job(jobId, file.getOriginalFilename(), chunks.size());
        jobRepository.save(job);

        chunkPublisher.publish(chunks, jobId, file.getOriginalFilename(), hasAnki, knownWords);
        return Map.of(
                "job_id", jobId,
                "filename", file.getOriginalFilename(),
                "total_chunks", chunks.size(),
                "status", "processing"
        );
    }
}