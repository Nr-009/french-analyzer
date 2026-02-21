package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChunkResultRepository extends JpaRepository<ChunkResult, String> {
    List<ChunkResult> findByJob_JobId(String jobId);
    List<ChunkResult> findByJob_JobIdOrderByChunkIndexAsc(String jobId);
}

