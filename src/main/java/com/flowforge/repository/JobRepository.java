package com.flowforge.repository;

import com.flowforge.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query(
            value = """
                SELECT *
                FROM jobs
                WHERE status = 'PENDING'
                  AND (
                      next_attempt_at IS NULL
                      OR next_attempt_at <= CURRENT_TIMESTAMP
                  )
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<Job> findNextPendingJobForUpdate();

    @Query(
            value = """
                SELECT *
                FROM jobs
                WHERE status = 'PROCESSING'
                  AND processing_started_at < :cutoff
                ORDER BY processing_started_at
                FOR UPDATE SKIP LOCKED
                """,
            nativeQuery = true
    )
    List<Job> findStaleProcessingJobsForUpdate(
            @Param("cutoff") Instant cutoff
    );
}