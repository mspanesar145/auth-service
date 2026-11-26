CREATE TABLE IF NOT EXISTS outbox_events (
     id UUID PRIMARY KEY,
     aggregate_id UUID NOT NULL,
     aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    next_retry_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_error TEXT,

    CONSTRAINT chk_outbox_status
    CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),

    CONSTRAINT chk_outbox_retry_count
    CHECK (retry_count >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_events (next_retry_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate_id
    ON outbox_events (aggregate_id);

CREATE INDEX IF NOT EXISTS idx_outbox_published_at
    ON outbox_events (published_at)
    WHERE status = 'PUBLISHED';