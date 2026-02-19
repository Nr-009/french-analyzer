package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chunk_results")
public class ChunkResult {

    @Id
    private String chunkId;

    @ManyToOne
    @JoinColumn(name = "job_id")  
    private Job job;

    private String filename;
    private int chunkIndex;
    private String chunkName;
    private double difficultyScore;

    public ChunkResult() {}

    public ChunkResult(Job job, int chunkIndex, String chunkName, double difficultyScore) {
        this.chunkId = job.getJobId() + "_" + chunkIndex;
        this.job = job;
        this.filename = job.getFilename();
        this.chunkIndex = chunkIndex;
        this.chunkName = chunkName;
        this.difficultyScore = difficultyScore;
    }

    public String getChunkId() { return chunkId; }
    public Job getJob() { return job; }
    public String getFilename() { return filename; }
    public int getChunkIndex() { return chunkIndex; }
    public String getChunkName() { return chunkName; }
    public double getDifficultyScore() { return difficultyScore; }
}