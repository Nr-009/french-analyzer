package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    private String jobId;
    private String filename;
    private String status;
    private int totalChunks;
    private int completedChunks;
    private LocalDateTime createdAt;

    public Job() {}

    public Job(String jobId, String filename, int totalChunks) {
        this.jobId = jobId;
        this.filename = filename;
        this.totalChunks = totalChunks;
        this.completedChunks = 0;
        this.status = "PROCESSING";
        this.createdAt = LocalDateTime.now();
    }

    public String getJobId() { return jobId; }
    public String getFilename() { return filename; }
    public String getStatus() { return status; }
    public int getTotalChunks() { return totalChunks; }
    public int getCompletedChunks() { return completedChunks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setCompletedChunks(int completedChunks) { this.completedChunks = completedChunks; }
}