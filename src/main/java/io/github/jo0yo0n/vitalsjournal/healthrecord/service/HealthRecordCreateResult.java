package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import java.util.List;

public record HealthRecordCreateResult(
    HealthRecord healthRecord, List<RecordViolation> violations) {}
