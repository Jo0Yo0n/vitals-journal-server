package io.github.jo0yo0n.vitalsjournal.recordviolation.controller.response;

import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.ViolationDirection;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import java.time.Instant;

public record RecordViolationResponse(
    Long id,
    Long healthRecordId,
    ThresholdMetric metric,
    Short measuredValue,
    Short minValueSnapshot,
    Short maxValueSnapshot,
    ViolationDirection direction,
    Instant evaluatedAt) {

  public static RecordViolationResponse from(RecordViolation violation) {
    return new RecordViolationResponse(
        violation.getId(),
        violation.getHealthRecord().getId(),
        violation.getMetric(),
        violation.getMeasuredValue(),
        violation.getMinValueSnapshot(),
        violation.getMaxValueSnapshot(),
        violation.getViolationDirection(),
        violation.getEvaluatedAt());
  }
}
