package com.flowforge.model;

import java.time.Instant;

public class Job {
    private long id;
    private String type;
    private String payload; //stats that job needs
    private JobStatus status;
    private Instant createdAt;

    public Job(long id, String type, String payload, JobStatus status, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
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
}
