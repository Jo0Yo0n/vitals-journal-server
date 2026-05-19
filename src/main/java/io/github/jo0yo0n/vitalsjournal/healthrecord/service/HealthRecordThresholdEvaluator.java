package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HealthRecordThresholdEvaluator {

  public List<RecordViolation> evaluate(HealthRecord record, List<Threshold> thresholds) {

    Map<ThresholdMetric, Threshold> thresholdByMetric =
        thresholds.stream().collect(Collectors.toMap(Threshold::getMetric, Function.identity()));

    List<RecordViolation> violations = new ArrayList<>();

    for (ThresholdMetric metric : record.getRecordType().thresholdMetrics()) {

      Threshold threshold = thresholdByMetric.get(metric);

      if (threshold == null) {
        continue;
      }

      Short measuredValue = measuredValueOf(record, metric);

      if (isBelowMin(measuredValue, threshold)) {
        violations.add(
            RecordViolation.ofBelowMin(record, metric, measuredValue, threshold.getMinValue()));
      } else if (isAboveMax(measuredValue, threshold)) {
        violations.add(
            RecordViolation.ofAboveMax(record, metric, measuredValue, threshold.getMaxValue()));
      }
    }
    return violations;
  }

  private Short measuredValueOf(HealthRecord record, ThresholdMetric metric) {
    return switch (metric) {
      case HR -> record.getBpm();
      case BP_SYS -> record.getSystolic();
      case BP_DIA -> record.getDiastolic();
    };
  }

  private boolean isBelowMin(Short measuredValue, Threshold threshold) {
    return threshold.getMinValue() != null && measuredValue < threshold.getMinValue();
  }

  private boolean isAboveMax(Short measuredValue, Threshold threshold) {
    return threshold.getMaxValue() != null && measuredValue > threshold.getMaxValue();
  }
}
