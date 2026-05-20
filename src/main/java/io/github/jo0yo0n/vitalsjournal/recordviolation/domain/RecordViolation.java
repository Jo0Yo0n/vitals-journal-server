package io.github.jo0yo0n.vitalsjournal.recordviolation.domain;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "record_violation")
public class RecordViolation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "health_record_id", nullable = false)
  private HealthRecord healthRecord;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric", nullable = false, length = 16)
  private ThresholdMetric metric;

  @Column(name = "measured_value", nullable = false)
  private Short measuredValue;

  @Column(name = "min_value_snapshot")
  private Short minValueSnapshot;

  @Column(name = "max_value_snapshot")
  private Short maxValueSnapshot;

  @Enumerated(EnumType.STRING)
  @Column(name = "direction", nullable = false, length = 16)
  private ViolationDirection violationDirection;

  @Column(name = "evaluated_at", nullable = false)
  private Instant evaluatedAt;

  public static RecordViolation ofBelowMin(
      HealthRecord healthRecord,
      ThresholdMetric metric,
      Short measuredValue,
      Short minValueSnapshot) {

    RecordViolation violation = new RecordViolation();
    violation.healthRecord = healthRecord;
    violation.metric = metric;
    violation.measuredValue = measuredValue;
    violation.minValueSnapshot = minValueSnapshot;
    violation.violationDirection = ViolationDirection.BELOW_MIN;
    violation.evaluatedAt = Instant.now();
    return violation;
  }

  public static RecordViolation ofAboveMax(
      HealthRecord healthRecord,
      ThresholdMetric metric,
      Short measuredValue,
      Short maxValueSnapshot) {

    RecordViolation violation = new RecordViolation();
    violation.healthRecord = healthRecord;
    violation.metric = metric;
    violation.measuredValue = measuredValue;
    violation.maxValueSnapshot = maxValueSnapshot;
    violation.violationDirection = ViolationDirection.ABOVE_MAX;
    violation.evaluatedAt = Instant.now();
    return violation;
  }

  public Long getId() {
    return id;
  }

  public HealthRecord getHealthRecord() {
    return healthRecord;
  }

  public ThresholdMetric getMetric() {
    return metric;
  }

  public Short getMeasuredValue() {
    return measuredValue;
  }

  public Short getMinValueSnapshot() {
    return minValueSnapshot;
  }

  public Short getMaxValueSnapshot() {
    return maxValueSnapshot;
  }

  public ViolationDirection getViolationDirection() {
    return violationDirection;
  }

  public Instant getEvaluatedAt() {
    return evaluatedAt;
  }
}
