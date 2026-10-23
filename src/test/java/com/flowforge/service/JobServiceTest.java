package com.flowforge.service;

import com.flowforge.dto.CreateJobRequest;
import com.flowforge.dto.CreateJobResult;
import com.flowforge.model.Job;
import com.flowforge.model.JobStatus;
import com.flowforge.repository.JobRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void shouldReturnExistingJobForDuplicateIdempotencyKey() {

        CreateJobRequest request =
                new CreateJobRequest(
                        "EMAIL",
                        "Test payload",
                        "idem-test-001"
                );

        Job existingJob =
                new Job(
                        "EMAIL",
                        "Test payload",
                        "idem-test-001"
                );

        when(
                jobRepository.findByIdempotencyKey(
                        "idem-test-001"
                )
        ).thenReturn(Optional.of(existingJob));

        CreateJobResult result =
                jobService.createJob(request);

        assertFalse(result.created());

        assertSame(
                existingJob,
                result.job()
        );

        verify(
                jobRepository,
                never()
        ).saveAndFlush(any(Job.class));
    }

    @Test
    void shouldCreateNewJobForNewIdempotencyKey() {

        CreateJobRequest request =
                new CreateJobRequest(
                        "EMAIL",
                        "Test payload",
                        "idem-test-002"
                );

        when(
                jobRepository.findByIdempotencyKey(
                        "idem-test-002"
                )
        ).thenReturn(Optional.empty());

        when(
                jobRepository.saveAndFlush(any(Job.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        CreateJobResult result =
                jobService.createJob(request);

        assertTrue(result.created());

        assertEquals(
                "EMAIL",
                result.job().getType()
        );

        assertEquals(
                "Test payload",
                result.job().getPayload()
        );

        assertEquals(
                "idem-test-002",
                result.job().getIdempotencyKey()
        );

        verify(
                jobRepository,
                times(1)
        ).saveAndFlush(any(Job.class));
    }

    @Test
    void shouldScheduleRetryWhenRetriesRemain() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "retry-test-001"
        );

        job.markProcessing();
        job.incrementAttemptCount();

        boolean willRetry =
                jobService.retryOrFailJob(
                        job,
                        "Temporary failure"
                );

        assertTrue(willRetry);

        assertEquals(
                JobStatus.PENDING,
                job.getStatus()
        );

        assertEquals(
                "Temporary failure",
                job.getLastError()
        );

        assertNotNull(
                job.getNextAttemptAt()
        );

        assertNull(
                job.getProcessingStartedAt()
        );

        assertNull(
                job.getHeartbeatAt()
        );

        verify(
                jobRepository,
                times(1)
        ).save(job);
    }

    @Test
    void shouldMoveJobToDeadLetterWhenRetriesExceeded() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "retry-test-002"
        );

        job.markProcessing();

        job.incrementAttemptCount();
        job.incrementAttemptCount();
        job.incrementAttemptCount();
        job.incrementAttemptCount();

        boolean willRetry =
                jobService.retryOrFailJob(
                        job,
                        "Permanent failure"
                );

        assertFalse(willRetry);

        assertEquals(
                JobStatus.DEAD_LETTER,
                job.getStatus()
        );

        assertEquals(
                "Permanent failure",
                job.getLastError()
        );

        assertNotNull(
                job.getDeadLetterAt()
        );

        assertNull(
                job.getNextAttemptAt()
        );

        assertNull(
                job.getProcessingStartedAt()
        );

        assertNull(
                job.getHeartbeatAt()
        );

        verify(
                jobRepository,
                times(1)
        ).save(job);
    }

    @Test
    void shouldRetryDeadLetterJob() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "dlq-retry-test-001"
        );

        job.markProcessing();

        job.incrementAttemptCount();
        job.incrementAttemptCount();
        job.incrementAttemptCount();
        job.incrementAttemptCount();

        job.markDeadLetter(
                "Permanent failure"
        );

        when(
                jobRepository.findById(1L)
        ).thenReturn(Optional.of(job));

        Optional<Job> result =
                jobService.retryDeadLetterJob(1L);

        assertTrue(result.isPresent());

        assertEquals(
                JobStatus.PENDING,
                result.get().getStatus()
        );

        assertEquals(
                0,
                result.get().getAttemptCount()
        );

        assertNull(
                result.get().getDeadLetterAt()
        );

        assertNull(
                result.get().getNextAttemptAt()
        );

        assertNull(
                result.get().getProcessingStartedAt()
        );

        assertNull(
                result.get().getHeartbeatAt()
        );

        verify(
                jobRepository,
                times(1)
        ).save(job);
    }
}
