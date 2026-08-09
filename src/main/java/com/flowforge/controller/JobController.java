package com.flowforge.controller;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.model.Job;
import com.flowforge.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody CreateJobRequest request) {
        Job job = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable long id) {
        return jobService.getJob(id).map(ResponseEntity::ok).
                orElseGet(() -> ResponseEntity.notFound().build());
    }
}
