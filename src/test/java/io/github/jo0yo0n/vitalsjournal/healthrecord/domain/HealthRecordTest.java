package io.github.jo0yo0n.vitalsjournal.healthrecord.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.HealthRecordTypeMismatchException;
import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.InvalidBloodPressureRangeException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthRecordTest {

  private final User user = User.of("test@example.com", "hashed-password", "TestUser");
  private final Instant measuredAt = Instant.parse("2026-05-11T10:00:00Z");

  @DisplayName("HR 기록은 bpm 값으로 생성할 수 있다")
  @Test
  void createHeartRateRecord() {
    HealthRecord record = HealthRecord.ofHeartRate(user, measuredAt, (short) 72, "morning");

    assertThat(record.getRecordType()).isEqualTo(HealthRecordType.HR);
    assertThat(record.getMeasuredAt()).isEqualTo(measuredAt);
    assertThat(record.getBpm()).isEqualTo((short) 72);
    assertThat(record.getSystolic()).isNull();
    assertThat(record.getDiastolic()).isNull();
    assertThat(record.getMemo()).isEqualTo("morning");
  }

  @DisplayName("BP 기록은 systolic과 diastolic 값으로 생성할 수 있다")
  @Test
  void createBloodPressureRecord() {
    HealthRecord record =
        HealthRecord.ofBloodPressure(user, measuredAt, (short) 120, (short) 80, "after lunch");

    assertThat(record.getRecordType()).isEqualTo(HealthRecordType.BP);
    assertThat(record.getMeasuredAt()).isEqualTo(measuredAt);
    assertThat(record.getBpm()).isNull();
    assertThat(record.getSystolic()).isEqualTo((short) 120);
    assertThat(record.getDiastolic()).isEqualTo((short) 80);
    assertThat(record.getMemo()).isEqualTo("after lunch");
  }

  @DisplayName("HR 기록에서 bpm이 없으면 예외가 발생한다")
  @Test
  void createHeartRateWithoutBpmThrowsException() {
    assertThatThrownBy(() -> HealthRecord.ofHeartRate(user, measuredAt, null, null))
        .isInstanceOf(HealthRecordTypeMismatchException.class);
  }

  @DisplayName("BP 기록에서 systolic이 없으면 예외가 발생한다")
  @Test
  void createBloodPressureWithoutSystolicThrowsException() {
    assertThatThrownBy(() -> HealthRecord.ofBloodPressure(user, measuredAt, null, (short) 80, null))
        .isInstanceOf(HealthRecordTypeMismatchException.class);
  }

  @DisplayName("BP 기록에서 diastolic이 없으면 예외가 발생한다")
  @Test
  void createBloodPressureWithoutDiastolicThrowsException() {
    assertThatThrownBy(
            () -> HealthRecord.ofBloodPressure(user, measuredAt, (short) 120, null, null))
        .isInstanceOf(HealthRecordTypeMismatchException.class);
  }

  @DisplayName("BP 기록에서 systolic이 diastolic보다 작거나 같으면 예외가 발생한다")
  @Test
  void createBloodPressureWithInvalidRangeThrowsException() {
    assertThatThrownBy(
            () -> HealthRecord.ofBloodPressure(user, measuredAt, (short) 80, (short) 80, null))
        .isInstanceOf(InvalidBloodPressureRangeException.class);
  }

  @DisplayName("사용자가 없으면 예외가 발생한다")
  @Test
  void createWithoutUserThrowsException() {
    assertThatThrownBy(() -> HealthRecord.ofHeartRate(null, measuredAt, (short) 72, null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("BP 기록에서 사용자가 없으면 예외가 발생한다")
  @Test
  void createBloodPressureWithoutUserThrowsException() {
    assertThatThrownBy(
            () -> HealthRecord.ofBloodPressure(null, measuredAt, (short) 120, (short) 80, null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("측정 시각이 없으면 예외가 발생한다")
  @Test
  void createWithoutMeasuredAtThrowsException() {
    assertThatThrownBy(() -> HealthRecord.ofHeartRate(user, null, (short) 72, null))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("BP 기록에서 측정 시각이 없으면 예외가 발생한다")
  @Test
  void createBloodPressureWithoutMeasuredAtThrowsException() {
    assertThatThrownBy(
            () -> HealthRecord.ofBloodPressure(user, null, (short) 120, (short) 80, null))
        .isInstanceOf(NullPointerException.class);
  }
}
