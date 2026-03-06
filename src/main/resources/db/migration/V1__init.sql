CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email CITEXT NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_users_email_length CHECK (char_length(email::text) <= 320)
);

CREATE UNIQUE INDEX ux_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;

CREATE TABLE health_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    record_type VARCHAR(16) NOT NULL,
    measured_at TIMESTAMPTZ NOT NULL,
    bpm SMALLINT NULL,
    systolic SMALLINT NULL,
    diastolic SMALLINT NULL,
    memo TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_health_record_record_type CHECK (record_type IN ('HR', 'BP')),
    CONSTRAINT ck_health_record_bpm CHECK (bpm IS NULL OR bpm BETWEEN 1 AND 300),
    CONSTRAINT ck_health_record_systolic CHECK (systolic IS NULL OR systolic BETWEEN 50 AND 300),
    CONSTRAINT ck_health_record_diastolic CHECK (diastolic IS NULL OR diastolic BETWEEN 30 AND 200),
    CONSTRAINT ck_health_record_bp_order CHECK (
        systolic IS NULL OR diastolic IS NULL OR systolic > diastolic
    ),
    CONSTRAINT ck_health_record_record_type_columns CHECK (
        (record_type = 'HR' AND bpm IS NOT NULL AND systolic IS NULL AND diastolic IS NULL)
        OR
        (record_type = 'BP' AND bpm IS NULL AND systolic IS NOT NULL AND diastolic IS NOT NULL)
    )
);

CREATE INDEX ix_health_record_user_measured_at_active
    ON health_record (user_id, measured_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_health_record_user_created_at_active
    ON health_record (user_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE TABLE threshold (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    metric VARCHAR(16) NOT NULL,
    rule_type VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_threshold_metric CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA')),
    CONSTRAINT ck_threshold_rule_type CHECK (rule_type IN ('range'))
);

CREATE UNIQUE INDEX ux_threshold_user_metric_rule_type_active
    ON threshold (user_id, metric, rule_type)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_threshold_user_metric_rule_type_active
    ON threshold (user_id, metric, rule_type)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_threshold_user_active
    ON threshold (user_id)
    WHERE deleted_at IS NULL;

CREATE TABLE threshold_range (
    threshold_id BIGINT PRIMARY KEY REFERENCES threshold (id) ON DELETE CASCADE,
    min_value NUMERIC(10, 2) NULL,
    max_value NUMERIC(10, 2) NULL,
    CONSTRAINT ck_threshold_range_min_max CHECK (
        min_value IS NULL OR max_value IS NULL OR min_value < max_value
    )
);

CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    health_record_id BIGINT NOT NULL REFERENCES health_record (id) ON DELETE CASCADE,
    is_silent BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX ux_alert_health_record_active
    ON alert (health_record_id)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_alert_created_at_active
    ON alert (created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_alert_health_record_active
    ON alert (health_record_id)
    WHERE deleted_at IS NULL;

CREATE TABLE record_violation (
    id BIGSERIAL PRIMARY KEY,
    health_record_id BIGINT NOT NULL REFERENCES health_record (id) ON DELETE CASCADE,
    threshold_id BIGINT NOT NULL REFERENCES threshold (id),
    metric VARCHAR(16) NOT NULL,
    rule_record_type VARCHAR(16) NOT NULL,
    measured_value NUMERIC(10, 2) NOT NULL,
    direction VARCHAR(16) NULL,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_record_violation_metric CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA')),
    CONSTRAINT ck_record_violation_rule_record_type CHECK (rule_record_type IN ('range')),
    CONSTRAINT ck_record_violation_direction CHECK (
        direction IS NULL OR direction IN ('below_min', 'above_max')
    )
);

CREATE UNIQUE INDEX ux_record_violation_record_threshold_active
    ON record_violation (health_record_id, threshold_id)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_record_violation_health_record_active
    ON record_violation (health_record_id)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_record_violation_threshold_active
    ON record_violation (threshold_id)
    WHERE deleted_at IS NULL;

CREATE INDEX ix_record_violation_evaluated_at_active
    ON record_violation (evaluated_at DESC)
    WHERE deleted_at IS NULL;

CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_refresh_token_token_hash
    ON refresh_token (token_hash);

CREATE INDEX ix_refresh_token_user_expires_active
    ON refresh_token (user_id, expires_at DESC)
    WHERE revoked_at IS NULL;

CREATE INDEX ix_refresh_token_expires_active
    ON refresh_token (expires_at)
    WHERE revoked_at IS NULL;
