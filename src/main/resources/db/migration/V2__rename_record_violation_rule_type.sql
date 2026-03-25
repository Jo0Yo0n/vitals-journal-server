ALTER TABLE record_violation
    RENAME COLUMN rule_record_type TO rule_type;

ALTER TABLE record_violation
    RENAME CONSTRAINT ck_record_violation_rule_record_type
    TO ck_record_violation_rule_type;
