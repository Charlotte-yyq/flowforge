package com.flowforge.worker;

import com.flowforge.model.Job;
import com.flowforge.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JobWorker {

    private final JobService jobService;

    public JobWorker(JobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(fixedDelay = 2000)
    public void processNextJob() {

        Optional<Job> optionalJob =
                jobService.claimNextPendingJob();

        if (optionalJob.isEmpty()) {
            return;
        }

        Job job = optionalJob.get();

        try {
            System.out.println(
                    "Processing Job #" + job.getId()
            );

            Thread.sleep(3000);

            jobService.completeJob(job);

            System.out.println(
                    "Completed Job #" + job.getId()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            jobService.failJob(job);

            System.out.println(
                    "Job #" + job.getId() + " failed"
            );
        }
    }
}