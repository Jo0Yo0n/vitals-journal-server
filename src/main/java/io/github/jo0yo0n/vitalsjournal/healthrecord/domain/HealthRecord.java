package io.github.jo0yo0n.vitalsjournal.healthrecord.domain;

import io.github.jo0yo0n.vitalsjournal.common.domain.CreatedTimeEntity;
import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.HealthRecordTypeMismatchException;
import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.InvalidBloodPressureRangeException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
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
import java.util.Objects;

@Entity
@Table(name = "health_record")
public class HealthRecord extends CreatedTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "record_type", nullable = false, length = 16)
  private HealthRecordType recordType;

  @Column(name = "measured_at", nullable = false)
  private Instant measuredAt;

  @Column(name = "bpm")
  private Short bpm;

  @Column(name = "systolic")
  private Short systolic;

  @Column(name = "diastolic")
  private Short diastolic;

  @Column(name = "memo", length = 500)
  private String memo;

  public static HealthRecord ofHeartRate(User user, Instant measuredAt, Short bpm, String memo) {

    if (bpm == null) {
      throw new HealthRecordTypeMismatchException(
          "For type HR, bpm is required and systolic/diastolic must be omitted");
    }

    Objects.requireNonNull(user, "User must not be null");
    Objects.requireNonNull(measuredAt, "MeasuredAt must not be null");

    HealthRecord record = new HealthRecord();
    record.user = user;
    record.recordType = HealthRecordType.HR;
    record.measuredAt = measuredAt;
    record.bpm = bpm;
    record.memo = memo;
    return record;
  }

  public static HealthRecord ofBloodPressure(
      User user, Instant measuredAt, Short systolic, Short diastolic, String memo) {

    if (systolic == null || diastolic == null) {
      throw new HealthRecordTypeMismatchException(
          "For type BP, systolic and diastolic are required and bpm must be omitted");
    }

    if (systolic <= diastolic) {
      throw new InvalidBloodPressureRangeException();
    }

    Objects.requireNonNull(user, "User must not be null");
    Objects.requireNonNull(measuredAt, "MeasuredAt must not be null");

    HealthRecord record = new HealthRecord();
    record.user = user;
    record.recordType = HealthRecordType.BP;
    record.measuredAt = measuredAt;
    record.systolic = systolic;
    record.diastolic = diastolic;
    record.memo = memo;
    return record;
  }

  public Long getId() {
    return id;
  }

  public HealthRecordType getRecordType() {
    return recordType;
  }

  public Instant getMeasuredAt() {
    return measuredAt;
  }

  public Short getBpm() {
    return bpm;
  }

  public Short getSystolic() {
    return systolic;
  }

  public Short getDiastolic() {
    return diastolic;
  }

  public String getMemo() {
    return memo;
  }
}
