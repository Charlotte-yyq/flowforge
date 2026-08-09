package com.flowforge.service;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import com.flowforge.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(CreateJobRequest request) {

        Job job = new Job(
                request.type(),
                request.payload(),
                JobStatus.PENDING,
                Instant.now()
        );

        return jobRepository.save(job);
    }

    public Optional<Job> getJob(Long id) {
        return jobRepository.findById(id);
    }

    @Transactional
    public Optional<Job> claimNextPendingJob() {

        Optional<Job> optionalJob =
                jobRepository.findNextPendingJobForUpdate();

        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();

            job.markProcessing();
            jobRepository.save(job);
        }

        return optionalJob;
    }

    @Transactional
    public void completeJob(Job job) {
        job.markCompleted();
        jobRepository.save(job);
    }

    @Transactional
    public void failJob(Job job) {
        job.markFailed();
        jobRepository.save(job);
    }
}