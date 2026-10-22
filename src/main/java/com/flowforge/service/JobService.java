package com.flowforge.service;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import com.flowforge.repository.JobRepository;
import com.flowforge.dto.CreateJobResult;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    private static final long STALE_JOB_TIMEOUT_SECONDS = 10;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public CreateJobResult createJob(CreateJobRequest request) {

        Optional<Job> existingJob = jobRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingJob.isPresent()) {return new CreateJobResult(existingJob.get(), false);}
        Job job = new Job(
                request.type(),
                request.payload(),
                request.idempotencyKey()
        );
        try{
            Job savedJob = jobRepository.saveAndFlush(job);
            return new CreateJobResult(savedJob, true);
        } catch (DataIntegrityViolationException e) {
            Job existingConcurrentJob = jobRepository.findByIdempotencyKey(request.idempotencyKey())
                    .orElseThrow(() -> e);
            return new CreateJobResult(existingConcurrentJob, false);
        }

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
            job.incrementAttemptCount();

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
    public void failJob(Job job, String errorMessage) {
        job.markFailed(errorMessage);
        jobRepository.save(job);
    }

    @Transactional
    public boolean retryOrFailJob(Job job, String errorMessage) {

        if (job.getAttemptCount() <= job.getMaxRetries()) {

            long delaySeconds =
                    (long) Math.pow(2, job.getAttemptCount());

            Instant nextAttemptAt =
                    Instant.now().plusSeconds(delaySeconds);

            job.scheduleRetry(nextAttemptAt, errorMessage);
            jobRepository.save(job);

            return true;
        }

        job.markDeadLetter(errorMessage);
        jobRepository.save(job);

        return false;
    }

    @Transactional
    public int recoverStaleJobs(){
        Instant cutoff = Instant.now().minusSeconds(STALE_JOB_TIMEOUT_SECONDS);
        List<Job> staleJobs = jobRepository.findStaleProcessingJobsForUpdate(cutoff);
        for (Job job : staleJobs) {
            if (job.getAttemptCount() <= job.getMaxRetries()) {
                job.scheduleRetry(Instant.now(), "Recovered after worker timeout");
            }else{
                job.markDeadLetter("Worker timeout and retry limit was exceeded");
            }
        }
        jobRepository.saveAll(staleJobs);
        return staleJobs.size();
    }

    @Transactional
    public void refreshHeartbeat(Long jobId) {
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {return;}
        Job job = optionalJob.get();
        if (job.getStatus() != JobStatus.PROCESSING){return;}
        job.markHeartbeat();
        jobRepository.save(job);
    }

    public List<Job> getDeadLetterJobs() {
        return jobRepository.findByStatusOrderByDeadLetterAtDesc(
                JobStatus.DEAD_LETTER
        );
    }

    @Transactional
    public Optional<Job> retryDeadLetterJob(Long id){
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {return Optional.empty();}
        Job job = optionalJob.get();
        if (job.getStatus() != JobStatus.DEAD_LETTER){return Optional.empty();}
        job.retryFromDeadLetter();
        jobRepository.save(job);
        return Optional.of(job);
    }
}