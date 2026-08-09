package com.flowforge.repository;

import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findFirstByStatusOrderByCreatedAtAsc(JobStatus status);
}