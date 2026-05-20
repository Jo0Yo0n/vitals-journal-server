package io.github.jo0yo0n.vitalsjournal.healthrecord.service.command;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecordType;
import java.time.Instant;

public record HealthRecordCreateCommand(
    HealthRecordType type,
    Instant measuredAt,
    Short bpm,
    Short systolic,
    Short diastolic,
    String memo) {

  public boolean isHeartRate() {
    return type == HealthRecordType.HR;
  }

  public boolean isBloodPressure() {
    return type == HealthRecordType.BP;
  }

  public boolean hasOnlyHeartRate() {
    return bpm != null && systolic == null && diastolic == null;
  }

  public boolean hasOnlyBloodPressure() {
    return bpm == null && systolic != null && diastolic != null;
  }
}
