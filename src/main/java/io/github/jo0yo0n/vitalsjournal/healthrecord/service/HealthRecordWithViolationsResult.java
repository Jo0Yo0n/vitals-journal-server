package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import java.util.List;

public record HealthRecordWithViolationsResult(
    HealthRecord healthRecord, List<RecordViolation> violations) {}
