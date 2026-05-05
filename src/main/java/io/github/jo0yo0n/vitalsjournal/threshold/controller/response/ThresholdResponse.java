package io.github.jo0yo0n.vitalsjournal.threshold.controller.response;

import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import java.math.BigDecimal;
import java.time.Instant;

public record ThresholdResponse(
    Long id, ThresholdMetric metric, BigDecimal minValue, BigDecimal maxValue, Instant updatedAt) {

  public static ThresholdResponse from(Threshold threshold) {
    return new ThresholdResponse(
        threshold.getId(),
        threshold.getMetric(),
        threshold.getMinValue(),
        threshold.getMaxValue(),
        threshold.getUpdatedAt());
  }
}
