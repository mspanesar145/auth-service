CREATE TABLE auth_users (
 id UUID PRIMARY KEY, phone_number VARCHAR(20) NOT NULL UNIQUE,
 phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
 status VARCHAR(30) NOT NULL, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE otp_challenges (
 id UUID PRIMARY KEY, phone_number VARCHAR(20) NOT NULL, code_hash VARCHAR(128) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL, attempts INTEGER NOT NULL DEFAULT 0,
 consumed BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_otp_phone_created ON otp_challenges(phone_number,created_at DESC);
CREATE TABLE refresh_sessions (
 id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES auth_users(id), token_hash VARCHAR(128) NOT NULL UNIQUE,
 device_name VARCHAR(100), expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_refresh_user ON refresh_sessions(user_id);

CREATE TABLE outbox_events (
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
                                   CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_pending
    ON outbox_events (next_retry_at, created_at)
    WHERE status = 'PENDING';