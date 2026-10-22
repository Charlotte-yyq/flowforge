package com.flowforge.controller;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.model.Job;
import com.flowforge.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flowforge.dto.CreateJobResult;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody CreateJobRequest request) {
        CreateJobResult result = jobService.createJob(request);
        if (result.created()){
            return ResponseEntity.status(HttpStatus.CREATED).body(result.job());
        }

        return ResponseEntity.ok(result.job());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable long id) {
        return jobService.getJob(id).map(ResponseEntity::ok).
                orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dead-letter")
    public ResponseEntity<List<Job>> getDeadLetterJobs() {

        List<Job> jobs =
                jobService.getDeadLetterJobs();

        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Job> retryDeadLetterJob(
            @PathVariable Long id
    ) {
        return jobService.retryDeadLetterJob(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }
}
