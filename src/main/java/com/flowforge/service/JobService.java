package com.flowforge.service;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JobService {
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Job createJob(CreateJobRequest request) {
        long id = idGenerator.getAndIncrement();
        Job job = new Job(id, request.type(), request.payload(), JobStatus.PENDING, Instant.now());
        jobs.put(id, job);
        return job;
    }

    public Optional<Job> getJob(long id) {
        return Optional.ofNullable(jobs.get(id));
    }
}
