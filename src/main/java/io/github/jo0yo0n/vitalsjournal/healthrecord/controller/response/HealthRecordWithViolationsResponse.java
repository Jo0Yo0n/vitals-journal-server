package io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.controller.response.RecordViolationResponse;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import java.util.List;

public record HealthRecordWithViolationsResponse(
    HealthRecordResponse healthRecord, List<RecordViolationResponse> violations) {

  public static HealthRecordWithViolationsResponse from(
      HealthRecord healthRecord, List<RecordViolation> violations) {

    return new HealthRecordWithViolationsResponse(
        HealthRecordResponse.from(healthRecord),
        violations.stream().map(RecordViolationResponse::from).toList());
  }
}
