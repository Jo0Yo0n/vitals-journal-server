package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.ViolationDirection;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthRecordThresholdEvaluatorTest {

  private final HealthRecordThresholdEvaluator evaluator = new HealthRecordThresholdEvaluator();
  private final User user = User.of("test@example.com", "hashed-password", "TestUser");
  private final Instant measuredAt = Instant.parse("2026-05-11T10:00:00Z");

  @DisplayName("HR 측정값이 minValue보다 작으면 BELOW_MIN 위반을 만든다")
  @Test
  void evaluateHeartRateBelowMin() {
    HealthRecord record = HealthRecord.ofHeartRate(user, measuredAt, (short) 50, null);
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, (short) 60, (short) 100);

    List<RecordViolation> violations = evaluator.evaluate(record, List.of(threshold));

    assertThat(violations).hasSize(1);
    assertViolation(
        violations.get(0),
        record,
        ThresholdMetric.HR,
        (short) 50,
        (short) 60,
        null,
        ViolationDirection.BELOW_MIN);
  }

  @DisplayName("HR 측정값이 maxValue보다 크면 ABOVE_MAX 위반을 만든다")
  @Test
  void evaluateHeartRateAboveMax() {
    HealthRecord record = HealthRecord.ofHeartRate(user, measuredAt, (short) 130, null);
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, (short) 60, (short) 120);

    List<RecordViolation> violations = evaluator.evaluate(record, List.of(threshold));

    assertThat(violations).hasSize(1);
    assertViolation(
        violations.get(0),
        record,
        ThresholdMetric.HR,
        (short) 130,
        null,
        (short) 120,
        ViolationDirection.ABOVE_MAX);
  }

  @DisplayName("BP 기록은 수축기와 이완기 임계값을 각각 평가한다")
  @Test
  void evaluateBloodPressureMetrics() {
    HealthRecord record =
        HealthRecord.ofBloodPressure(user, measuredAt, (short) 150, (short) 50, null);
    Threshold systolicThreshold = Threshold.of(user, ThresholdMetric.BP_SYS, null, (short) 140);
    Threshold diastolicThreshold = Threshold.of(user, ThresholdMetric.BP_DIA, (short) 60, null);

    List<RecordViolation> violations =
        evaluator.evaluate(record, List.of(systolicThreshold, diastolicThreshold));

    assertThat(violations).hasSize(2);
    assertViolation(
        violations.get(0),
        record,
        ThresholdMetric.BP_SYS,
        (short) 150,
        null,
        (short) 140,
        ViolationDirection.ABOVE_MAX);
    assertViolation(
        violations.get(1),
        record,
        ThresholdMetric.BP_DIA,
        (short) 50,
        (short) 60,
        null,
        ViolationDirection.BELOW_MIN);
  }

  @DisplayName("해당 metric의 임계값이 없으면 위반을 만들지 않는다")
  @Test
  void evaluateWithoutMatchingThreshold() {
    HealthRecord record = HealthRecord.ofHeartRate(user, measuredAt, (short) 130, null);
    Threshold threshold = Threshold.of(user, ThresholdMetric.BP_SYS, null, (short) 120);

    List<RecordViolation> violations = evaluator.evaluate(record, List.of(threshold));

    assertThat(violations).isEmpty();
  }

  @DisplayName("측정값이 임계값 범위 안이면 위반을 만들지 않는다")
  @Test
  void evaluateWithinThresholdRange() {
    HealthRecord record = HealthRecord.ofHeartRate(user, measuredAt, (short) 72, null);
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, (short) 60, (short) 100);

    List<RecordViolation> violations = evaluator.evaluate(record, List.of(threshold));

    assertThat(violations).isEmpty();
  }

  private void assertViolation(
      RecordViolation violation,
      HealthRecord healthRecord,
      ThresholdMetric metric,
      Short measuredValue,
      Short minValueSnapshot,
      Short maxValueSnapshot,
      ViolationDirection direction) {

    assertThat(violation.getHealthRecord()).isSameAs(healthRecord);
    assertThat(violation.getMetric()).isEqualTo(metric);
    assertThat(violation.getMeasuredValue()).isEqualTo(measuredValue);
    assertThat(violation.getMinValueSnapshot()).isEqualTo(minValueSnapshot);
    assertThat(violation.getMaxValueSnapshot()).isEqualTo(maxValueSnapshot);
    assertThat(violation.getViolationDirection()).isEqualTo(direction);
    assertThat(violation.getEvaluatedAt()).isNotNull();
  }
}
