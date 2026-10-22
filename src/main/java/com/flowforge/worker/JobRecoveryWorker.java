package com.flowforge.worker;

import com.flowforge.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobRecoveryWorker {

    private final JobService jobService;

    public JobRecoveryWorker(JobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(fixedDelay = 5000)
    public void recoverStaleJobs() {

        int recoveredJobs =
                jobService.recoverStaleJobs();

        if (recoveredJobs > 0) {
            System.out.println(
                    "Handled "
                            + recoveredJobs
                            + " stale job(s)"
            );
        }
    }
}
