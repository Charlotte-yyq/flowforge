package com.flowforge.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void shouldMarkJobAsProcessing() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "test-key-001"
        );

        job.markProcessing();

        assertEquals(
                JobStatus.PROCESSING,
                job.getStatus()
        );

        assertNotNull(
                job.getProcessingStartedAt()
        );

        assertNotNull(
                job.getHeartbeatAt()
        );
    }

    @Test
    void shouldMarkJobAsCompleted() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "test-key-002"
        );

        job.markProcessing();
        job.markCompleted();

        assertEquals(
                JobStatus.COMPLETED,
                job.getStatus()
        );

        assertNull(job.getProcessingStartedAt());
        assertNull(job.getHeartbeatAt());
    }

    @Test
    void shouldMarkJobAsDeadLetter() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "test-key-003"
        );

        job.markProcessing();

        job.markDeadLetter("Test failure");

        assertEquals(
                JobStatus.DEAD_LETTER,
                job.getStatus()
        );

        assertEquals(
                "Test failure",
                job.getLastError()
        );

        assertNotNull(job.getDeadLetterAt());
        assertNull(job.getHeartbeatAt());
        assertNull(job.getProcessingStartedAt());
        assertNull(job.getNextAttemptAt());
    }

    @Test
    void shouldRetryJobFromDeadLetter() {

        Job job = new Job(
                "EMAIL",
                "Test payload",
                "test-key-004"
        );

        job.markProcessing();
        job.incrementAttemptCount();

        job.markDeadLetter("Test failure");

        job.retryFromDeadLetter();

        assertEquals(
                JobStatus.PENDING,
                job.getStatus()
        );

        assertEquals(
                0,
                job.getAttemptCount()
        );

        assertNull(job.getDeadLetterAt());
        assertNull(job.getHeartbeatAt());
        assertNull(job.getProcessingStartedAt());
        assertNull(job.getNextAttemptAt());
    }
}