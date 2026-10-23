# FlowForge

FlowForge is a backend job processing system built with Java, Spring Boot, and PostgreSQL. It provides reliable asynchronous job execution with retry handling, concurrent worker coordination, crash recovery, idempotency, and dead-letter queue support.

## Features

- REST API for creating and retrieving background jobs
- PostgreSQL-backed persistent job queue
- Concurrent-safe job claiming using `FOR UPDATE SKIP LOCKED`
- Multiple worker support without duplicate job claiming
- Automatic retries with exponential backoff
- Worker heartbeat and stale-job recovery
- Idempotent job creation using unique idempotency keys
- Dead Letter Queue (DLQ) for permanently failed jobs
- Manual retry of dead-letter jobs
- Flyway database migrations
- JUnit and Mockito automated tests
- GitHub Actions CI with PostgreSQL integration

## Tech Stack

- Java 21+
- Spring Boot 4
- Spring Web
- Spring Data JPA
- PostgreSQL 17
- Flyway
- JUnit
- Mockito
- Maven
- GitHub Actions

## Job Lifecycle

```text
                     ┌─────────────┐
                     │   PENDING   │
                     └──────┬──────┘
                            │ Worker claims job
                            ▼
                     ┌─────────────┐
                     │ PROCESSING  │
                     └──────┬──────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
          ┌───────────┐           Execution fails
          │ COMPLETED │                  │
          └───────────┘                  ▼
                                  Retries remaining?
                                   │           │
                                  Yes          No
                                   │           │
                                   ▼           ▼
                               PENDING    DEAD_LETTER
```

## Reliability Features

### Concurrent Job Claiming

Workers claim jobs using PostgreSQL row-level locking:

```sql
SELECT *
FROM jobs
WHERE status = 'PENDING'
  AND (
      next_attempt_at IS NULL
      OR next_attempt_at <= CURRENT_TIMESTAMP
  )
ORDER BY created_at
FOR UPDATE SKIP LOCKED
LIMIT 1;
```

This allows multiple FlowForge workers to process jobs concurrently without claiming the same job.

### Retry and Exponential Backoff

Failed jobs are automatically retried. Retry delays increase exponentially between attempts.

A job configured with three retries may execute up to four times:

```text
Attempt 1
→ retry

Attempt 2
→ retry

Attempt 3
→ retry

Attempt 4
→ DEAD_LETTER if it still fails
```

### Worker Heartbeat and Crash Recovery

While processing long-running jobs, workers periodically update a heartbeat timestamp.

If a worker crashes and the heartbeat becomes stale, FlowForge detects the abandoned `PROCESSING` job and returns it to the queue.

This prevents jobs from remaining permanently stuck after a worker failure.

### Idempotency

Clients can supply an `idempotencyKey` when creating a job.

Repeated requests with the same key return the original job instead of creating duplicates.

The database also enforces a unique constraint on the idempotency key to protect against concurrent duplicate requests.

### Dead Letter Queue

Jobs that exceed the retry limit are moved to the `DEAD_LETTER` state.

Dead-letter jobs can be inspected through:

```http
GET /api/jobs/dead-letter
```

They can also be manually returned to the queue:

```http
POST /api/jobs/{id}/retry
```

## API

### Create a Job

```http
POST /api/jobs
Content-Type: application/json
```

Example:

```json
{
  "type": "EMAIL",
  "payload": "Send welcome email",
  "idempotencyKey": "welcome-email-user-123"
}
```

A newly created job returns `201 Created`.

If the same idempotency key has already been used, FlowForge returns the existing job with `200 OK`.

### Get a Job

```http
GET /api/jobs/{id}
```

### List Dead-Letter Jobs

```http
GET /api/jobs/dead-letter
```

### Retry a Dead-Letter Job

```http
POST /api/jobs/{id}/retry
```

## Database Migrations

FlowForge uses Flyway to version database schema changes.

Migration files are stored in:

```text
src/main/resources/db/migration
```

Current migrations include:

```text
V1__create_jobs_table.sql
V2__add_job_queue_indexes.sql
```

## Testing

Unit tests cover core job lifecycle and service behavior, including:

- Processing and completion transitions
- Dead-letter transitions
- Manual DLQ retries
- Idempotent job creation
- Retry scheduling
- Retry exhaustion

Run tests locally with:

```bash
./mvnw test
```

## Continuous Integration

GitHub Actions automatically runs the test suite on pushes and pull requests.

The CI environment:

1. Starts a PostgreSQL 17 container
2. Sets up Java
3. Applies Flyway migrations
4. Runs the Maven test suite

This verifies that FlowForge can start from a clean database and pass its automated tests in a fresh environment.

## Running Locally

Create a PostgreSQL database and user for FlowForge, then provide the database password through the environment variable:

```bash
export FLOWFORGE_DB_PASSWORD=your_password
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API runs by default at:

```text
http://localhost:8080
```

## Project Goal

FlowForge was built to explore backend reliability and distributed job-processing concepts, including concurrency control, retry strategies, idempotency, failure recovery, database migrations, and automated CI testing.

## Architecture
```text
Client
↓
REST API
↓
PostgreSQL Job Queue
↓
Multiple Workers
↓
Retry / Heartbeat / Recovery / DLQ
```