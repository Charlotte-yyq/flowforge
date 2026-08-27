package com.flowforge.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String payload;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private Instant createdAt;

    private Integer attemptCount = 0;

    private Integer maxRetries = 3;

    private Instant nextAttemptAt;

    private String lastError;

    private Instant processingStartedAt;

    protected Job() {
    }

    public Job(String type, String payload, JobStatus status, Instant createdAt) {
        this.type = type;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markProcessing() {
        this.status = JobStatus.PROCESSING;
        this.nextAttemptAt = null;
        this.processingStartedAt = Instant.now();
    }

    public void markPending() {
        this.status = JobStatus.PENDING;
    }

    public void markCompleted(){
        this.status = JobStatus.COMPLETED;
        this.processingStartedAt = null;
    }

    public void markFailed(String errorMessage) {

        this.status = JobStatus.FAILED;
        this.lastError = errorMessage;
        this.processingStartedAt = null;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void incrementAttemptCount() {
        if (attemptCount == null) {
            attemptCount = 0;
        }

        attemptCount++;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt, String errorMessage) {
        this.status = JobStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = errorMessage;
        this.processingStartedAt = null;
    }
}