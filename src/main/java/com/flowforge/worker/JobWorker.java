package com.flowforge.worker;

import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import com.flowforge.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JobWorker {

    private final JobRepository jobRepository;

    public JobWorker(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelay = 2000)
    public void processNextJob() {

        Optional<Job> optionalJob =
                jobRepository.findFirstByStatusOrderByCreatedAtAsc(
                        JobStatus.PENDING
                );

        if (optionalJob.isEmpty()) {
            return;
        }

        Job job = optionalJob.get();

        try {
            job.markProcessing();
            jobRepository.save(job);

            System.out.println(
                    "Processing Job #" + job.getId()
            );

            Thread.sleep(3000);

            job.markCompleted();
            jobRepository.save(job);

            System.out.println(
                    "Completed Job #" + job.getId()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            job.markFailed();
            jobRepository.save(job);

            System.out.println(
                    "Job #" + job.getId() + " failed"
            );
        }
    }
}