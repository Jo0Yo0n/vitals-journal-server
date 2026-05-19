package io.github.jo0yo0n.vitalsjournal.healthrecord.controller.response;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.controller.response.RecordViolationResponse;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import java.util.List;

public record HealthRecordCreateResponse(
    HealthRecordResponse healthRecord, List<RecordViolationResponse> violations) {

  public static HealthRecordCreateResponse from(
      HealthRecord healthRecord, List<RecordViolation> violations) {

    return new HealthRecordCreateResponse(
        HealthRecordResponse.from(healthRecord),
        violations.stream().map(RecordViolationResponse::from).toList());
  }
}
