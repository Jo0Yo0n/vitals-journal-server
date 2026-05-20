package io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecordType;
import java.time.Instant;

public record HealthRecordResponse(
    Long id,
    HealthRecordType type,
    Instant measuredAt,
    Short bpm,
    Short systolic,
    Short diastolic,
    String memo,
    Instant createdAt) {

  public static HealthRecordResponse from(HealthRecord healthRecord) {
    return new HealthRecordResponse(
        healthRecord.getId(),
        healthRecord.getRecordType(),
        healthRecord.getMeasuredAt(),
        healthRecord.getBpm(),
        healthRecord.getSystolic(),
        healthRecord.getDiastolic(),
        healthRecord.getMemo(),
        healthRecord.getCreatedAt());
  }
}
