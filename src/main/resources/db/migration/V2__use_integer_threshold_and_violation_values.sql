ALTER TABLE threshold
ALTER COLUMN min_value TYPE SMALLINT USING min_value::SMALLINT,
    ALTER COLUMN max_value TYPE SMALLINT USING max_value::SMALLINT;
ALTER TABLE record_violation DROP CONSTRAINT ck_record_violation_direction,
    DROP CONSTRAINT ck_record_violation_direction_required_bound;
ALTER TABLE record_violation
ALTER COLUMN measured_value TYPE SMALLINT USING measured_value::SMALLINT,
    ALTER COLUMN min_value_snapshot TYPE SMALLINT USING min_value_snapshot::SMALLINT,
    ALTER COLUMN max_value_snapshot TYPE SMALLINT USING max_value_snapshot::SMALLINT;
ALTER TABLE record_violation
ADD CONSTRAINT ck_record_violation_direction CHECK (
        direction IN ('BELOW_MIN', 'ABOVE_MAX')
    ),
    ADD CONSTRAINT ck_record_violation_direction_required_bound CHECK (
        (
            direction = 'BELOW_MIN'
            AND min_value_snapshot IS NOT NULL
        )
        OR (
            direction = 'ABOVE_MAX'
            AND max_value_snapshot IS NOT NULL
        )
    );
