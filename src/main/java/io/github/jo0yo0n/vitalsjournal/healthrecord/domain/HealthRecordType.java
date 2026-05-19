package io.github.jo0yo0n.vitalsjournal.healthrecord.domain;

import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import java.util.List;

public enum HealthRecordType {
  HR,
  BP;

  public List<ThresholdMetric> thresholdMetrics() {
    return switch (this) {
      case HR -> List.of(ThresholdMetric.HR);
      case BP -> List.of(ThresholdMetric.BP_SYS, ThresholdMetric.BP_DIA);
    };
  }
}
