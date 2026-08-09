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

    public void markProcessing(){
        this.status = JobStatus.PROCESSING;
    }

    public void markCompleted(){
        this.status = JobStatus.COMPLETED;
    }

    public void markFailed(){
        this.status = JobStatus.FAILED;
    }
}