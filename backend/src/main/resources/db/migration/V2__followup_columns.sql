-- Tracks delivery of the welcome email (sent on submission) and the
-- day-7 follow-up. Both nullable because email capture is optional;
-- the day-7 cron only acts on sessions where welcome_sent_at is set
-- and day7_sent_at is null and created_at is >= 7 days ago.

ALTER TABLE mri_sessions
    ADD COLUMN welcome_sent_at TIMESTAMPTZ,
    ADD COLUMN day7_sent_at    TIMESTAMPTZ,
    ADD COLUMN email           VARCHAR(255);

CREATE INDEX idx_sessions_email ON mri_sessions(email);

-- Lets the cron pull only candidate rows efficiently.
CREATE INDEX idx_sessions_day7_due ON mri_sessions(created_at)
    WHERE welcome_sent_at IS NOT NULL AND day7_sent_at IS NULL;
