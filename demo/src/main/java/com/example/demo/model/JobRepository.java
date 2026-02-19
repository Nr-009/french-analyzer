package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.completedChunks = j.completedChunks + 1 WHERE j.jobId = :jobId")
    void incrementCompletedChunks(@Param("jobId") String jobId);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.status = :status WHERE j.jobId = :jobId")
    void updateStatus(@Param("jobId") String jobId, @Param("status") String status);    
}