CREATE INDEX IF NOT EXISTS idx_jobs_pending_lookup
    ON jobs (status, next_attempt_at, created_at);

CREATE INDEX IF NOT EXISTS idx_jobs_stale_processing_lookup
    ON jobs (status, heartbeat_at);

CREATE INDEX IF NOT EXISTS idx_jobs_dead_letter_lookup
    ON jobs (status, dead_letter_at);