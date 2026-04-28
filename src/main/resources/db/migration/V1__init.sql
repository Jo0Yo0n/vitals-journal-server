CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email CITEXT NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_users_email_length CHECK (char_length(email::text) <= 320),
    CONSTRAINT ck_users_nickname_length CHECK (
        char_length(nickname) >= 1 AND char_length(nickname) <= 50
    ),
    CONSTRAINT ck_users_nickname_not_blank CHECK (
        char_length(btrim(nickname)) >= 1
    )
);

CREATE UNIQUE INDEX ux_users_email
    ON users (email);

CREATE UNIQUE INDEX ux_users_nickname
    ON users (nickname);


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

    CONSTRAINT ck_health_record_record_type CHECK (record_type IN ('HR', 'BP')),
    CONSTRAINT ck_health_record_bpm_range CHECK (
        bpm IS NULL OR bpm BETWEEN 1 AND 300
    ),
    CONSTRAINT ck_health_record_systolic_range CHECK (
        systolic IS NULL OR systolic BETWEEN 50 AND 300
    ),
    CONSTRAINT ck_health_record_diastolic_range CHECK (
        diastolic IS NULL OR diastolic BETWEEN 30 AND 200
    ),
    CONSTRAINT ck_health_record_memo_length CHECK (
        memo IS NULL OR char_length(memo) <= 500
    ),
    CONSTRAINT ck_health_record_bp_order CHECK (
        systolic IS NULL OR diastolic IS NULL OR systolic > diastolic
    ),
    CONSTRAINT ck_health_record_record_type_columns CHECK (
        (
            record_type = 'HR'
            AND bpm IS NOT NULL
            AND systolic IS NULL
            AND diastolic IS NULL
        )
        OR
        (
            record_type = 'BP'
            AND bpm IS NULL
            AND systolic IS NOT NULL
            AND diastolic IS NOT NULL
        )
    )
);

CREATE INDEX ix_health_record_user_measured_at
    ON health_record (user_id, measured_at DESC);

CREATE INDEX ix_health_record_user_created_at
    ON health_record (user_id, created_at DESC);

ALTER TABLE health_record
    ADD CONSTRAINT uq_health_record_id_user
        UNIQUE (id, user_id);


CREATE TABLE threshold (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    metric VARCHAR(16) NOT NULL,
    min_value NUMERIC(10, 2) NULL,
    max_value NUMERIC(10, 2) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_threshold_metric CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA')),
    CONSTRAINT ck_threshold_at_least_one_bound CHECK (
        min_value IS NOT NULL OR max_value IS NOT NULL
    ),
    CONSTRAINT ck_threshold_min_max CHECK (
        min_value IS NULL OR max_value IS NULL OR min_value <= max_value
    )
);

CREATE UNIQUE INDEX ux_threshold_user_metric
    ON threshold (user_id, metric);

CREATE INDEX ix_threshold_user
    ON threshold (user_id);


CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    health_record_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    read_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_alert_read_at_not_before_created_at CHECK (
        read_at IS NULL OR read_at >= created_at
    ),

    CONSTRAINT fk_alert_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_alert_health_record_user
        FOREIGN KEY (health_record_id, user_id)
            REFERENCES health_record (id, user_id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX ux_alert_health_record
    ON alert (health_record_id);

CREATE INDEX ix_alert_user_created_at
    ON alert (user_id, created_at DESC);


CREATE TABLE record_violation (
    id BIGSERIAL PRIMARY KEY,
    health_record_id BIGINT NOT NULL REFERENCES health_record (id) ON DELETE CASCADE,
    metric VARCHAR(16) NOT NULL,
    measured_value NUMERIC(10, 2) NOT NULL,
    min_value_snapshot NUMERIC(10, 2) NULL,
    max_value_snapshot NUMERIC(10, 2) NULL,
    direction VARCHAR(16) NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_record_violation_metric CHECK (metric IN ('HR', 'BP_SYS', 'BP_DIA')),
    CONSTRAINT ck_record_violation_direction CHECK (
        direction IN ('below_min', 'above_max')
    ),
    CONSTRAINT ck_record_violation_direction_required_bound CHECK (
        (direction = 'below_min' AND min_value_snapshot IS NOT NULL)
        OR
        (direction = 'above_max' AND max_value_snapshot IS NOT NULL)
    ),
    CONSTRAINT ck_record_violation_at_least_one_bound CHECK (
        min_value_snapshot IS NOT NULL OR max_value_snapshot IS NOT NULL
    ),
    CONSTRAINT ck_record_violation_min_max_snapshot CHECK (
        min_value_snapshot IS NULL
        OR max_value_snapshot IS NULL
        OR min_value_snapshot <= max_value_snapshot
    )
);

CREATE UNIQUE INDEX ux_record_violation_record_metric
    ON record_violation (health_record_id, metric);

CREATE INDEX ix_record_violation_health_record
    ON record_violation (health_record_id);

CREATE INDEX ix_record_violation_evaluated_at
    ON record_violation (evaluated_at DESC);
