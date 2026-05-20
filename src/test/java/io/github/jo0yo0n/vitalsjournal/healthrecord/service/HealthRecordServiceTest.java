package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.jo0yo0n.vitalsjournal.alert.repository.AlertRepository;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecordType;
import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.HealthRecordNotFoundException;
import io.github.jo0yo0n.vitalsjournal.healthrecord.exception.HealthRecordTypeMismatchException;
import io.github.jo0yo0n.vitalsjournal.healthrecord.repository.HealthRecordRepository;
import io.github.jo0yo0n.vitalsjournal.healthrecord.service.command.HealthRecordCreateCommand;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import io.github.jo0yo0n.vitalsjournal.recordviolation.repository.RecordViolationRepository;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.threshold.repository.ThresholdRepository;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class HealthRecordServiceTest {

  @Mock private HealthRecordRepository healthRecordRepository;
  @Mock private UserRepository userRepository;
  @Mock private ThresholdRepository thresholdRepository;
  @Mock private RecordViolationRepository recordViolationRepository;
  @Mock private AlertRepository alertRepository;
  @Mock private HealthRecordThresholdEvaluator thresholdEvaluator;

  @Captor private ArgumentCaptor<Iterable<RecordViolation>> violationCaptor;

  private HealthRecordService healthRecordService;

  @BeforeEach
  void setUp() {
    healthRecordService =
        new HealthRecordService(
            healthRecordRepository,
            userRepository,
            thresholdRepository,
            recordViolationRepository,
            alertRepository,
            thresholdEvaluator);
  }

  @DisplayName("사용자 ID로 건강 기록 목록을 최신 measuredAt 순으로 조회한다")
  @Test
  void getHealthRecordsByUserId() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord record =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, null);

    given(healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(1L)).willReturn(List.of(record));

    List<HealthRecord> result = healthRecordService.getHealthRecordsByUserId(1L);

    assertThat(result).containsExactly(record);
    then(healthRecordRepository).should().findByUserIdOrderByMeasuredAtDesc(1L);
  }

  @DisplayName("건강 기록 ID와 사용자 ID로 건강 기록과 위반 내역을 조회한다")
  @Test
  void getHealthRecord() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecord healthRecord =
        HealthRecord.ofHeartRate(user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null);
    RecordViolation violation =
        RecordViolation.ofAboveMax(healthRecord, ThresholdMetric.HR, (short) 130, (short) 120);

    given(healthRecordRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(healthRecord));
    given(recordViolationRepository.findByHealthRecordIdOrderByIdAsc(10L))
        .willReturn(List.of(violation));

    HealthRecordWithViolationsResult result = healthRecordService.getHealthRecord(10L, 1L);

    assertThat(result.healthRecord()).isEqualTo(healthRecord);
    assertThat(result.violations()).containsExactly(violation);
    then(healthRecordRepository).should().findByIdAndUserId(10L, 1L);
    then(recordViolationRepository).should().findByHealthRecordIdOrderByIdAsc(10L);
  }

  @DisplayName("건강 기록이 없으면 위반 내역을 조회하지 않고 예외를 던진다")
  @Test
  void getHealthRecordNotFound() {
    given(healthRecordRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> healthRecordService.getHealthRecord(10L, 1L))
        .isInstanceOf(HealthRecordNotFoundException.class);

    then(healthRecordRepository).should().findByIdAndUserId(10L, 1L);
    then(recordViolationRepository).should(never()).findByHealthRecordIdOrderByIdAsc(any());
  }

  @DisplayName("HR 건강 기록을 저장하고 HR metric 기준으로 임계값을 평가한다")
  @Test
  void saveHealthRecordCreatesHeartRateRecord() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            HealthRecordType.HR,
            Instant.parse("2026-05-11T10:00:00Z"),
            (short) 72,
            null,
            null,
            "morning");
    List<Threshold> thresholds = List.of(Threshold.of(user, ThresholdMetric.HR, (short) 60, null));

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(thresholdRepository.findByUserIdAndMetricIn(1L, List.of(ThresholdMetric.HR)))
        .willReturn(thresholds);
    given(thresholdEvaluator.evaluate(any(HealthRecord.class), eq(thresholds)))
        .willReturn(List.of());

    HealthRecordWithViolationsResult result = healthRecordService.saveHealthRecord(1L, command);
    HealthRecord healthRecord = result.healthRecord();

    assertThat(healthRecord.getRecordType()).isEqualTo(HealthRecordType.HR);
    assertThat(healthRecord.getMeasuredAt()).isEqualTo(Instant.parse("2026-05-11T10:00:00Z"));
    assertThat(healthRecord.getBpm()).isEqualTo((short) 72);
    assertThat(healthRecord.getSystolic()).isNull();
    assertThat(healthRecord.getDiastolic()).isNull();
    assertThat(healthRecord.getMemo()).isEqualTo("morning");
    assertThat(result.violations()).isEmpty();

    then(userRepository).should().findById(1L);
    then(thresholdRepository).should().findByUserIdAndMetricIn(1L, List.of(ThresholdMetric.HR));
    then(thresholdEvaluator).should().evaluate(healthRecord, thresholds);
    then(healthRecordRepository).should().save(healthRecord);
    then(recordViolationRepository).should(never()).saveAll(any());
    then(alertRepository).should(never()).save(any());
  }

  @DisplayName("BP 건강 기록을 저장하고 수축기/이완기 metric 기준으로 임계값을 평가한다")
  @Test
  void saveHealthRecordCreatesBloodPressureRecord() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            HealthRecordType.BP,
            Instant.parse("2026-05-11T10:00:00Z"),
            null,
            (short) 120,
            (short) 80,
            null);
    List<Threshold> thresholds =
        List.of(
            Threshold.of(user, ThresholdMetric.BP_SYS, null, (short) 130),
            Threshold.of(user, ThresholdMetric.BP_DIA, null, (short) 90));

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(
            thresholdRepository.findByUserIdAndMetricIn(
                1L, List.of(ThresholdMetric.BP_SYS, ThresholdMetric.BP_DIA)))
        .willReturn(thresholds);
    given(thresholdEvaluator.evaluate(any(HealthRecord.class), eq(thresholds)))
        .willReturn(List.of());

    HealthRecordWithViolationsResult result = healthRecordService.saveHealthRecord(1L, command);
    HealthRecord healthRecord = result.healthRecord();

    assertThat(healthRecord.getRecordType()).isEqualTo(HealthRecordType.BP);
    assertThat(healthRecord.getBpm()).isNull();
    assertThat(healthRecord.getSystolic()).isEqualTo((short) 120);
    assertThat(healthRecord.getDiastolic()).isEqualTo((short) 80);
    assertThat(result.violations()).isEmpty();

    then(thresholdRepository)
        .should()
        .findByUserIdAndMetricIn(1L, List.of(ThresholdMetric.BP_SYS, ThresholdMetric.BP_DIA));
    then(thresholdEvaluator).should().evaluate(healthRecord, thresholds);
    then(healthRecordRepository).should().save(healthRecord);
    then(recordViolationRepository).should(never()).saveAll(any());
    then(alertRepository).should(never()).save(any());
  }

  @DisplayName("임계값 위반이 있으면 건강 기록과 위반 내역, 알림을 저장한다")
  @Test
  void saveHealthRecordCreatesViolationAndAlert() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            HealthRecordType.HR,
            Instant.parse("2026-05-11T10:00:00Z"),
            (short) 130,
            null,
            null,
            null);
    List<Threshold> thresholds = List.of(Threshold.of(user, ThresholdMetric.HR, null, (short) 120));

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(thresholdRepository.findByUserIdAndMetricIn(1L, List.of(ThresholdMetric.HR)))
        .willReturn(thresholds);

    given(thresholdEvaluator.evaluate(any(HealthRecord.class), eq(thresholds)))
        .willAnswer(
            invocation -> {
              HealthRecord healthRecord = invocation.getArgument(0);
              return List.of(
                  RecordViolation.ofAboveMax(
                      healthRecord, ThresholdMetric.HR, (short) 130, (short) 120));
            });
    given(recordViolationRepository.saveAll(any()))
        .willAnswer(invocation -> invocation.getArgument(0));

    HealthRecordWithViolationsResult result = healthRecordService.saveHealthRecord(1L, command);
    HealthRecord healthRecord = result.healthRecord();

    then(healthRecordRepository).should().save(healthRecord);
    then(recordViolationRepository).should().saveAll(violationCaptor.capture());
    then(alertRepository).should().save(any());

    assertThat(result.violations()).hasSize(1);
    assertThat(violationCaptor.getValue()).hasSize(1);
  }

  @DisplayName("사용자가 없으면 건강 기록을 저장하지 않는다")
  @Test
  void saveHealthRecordUserNotFound() {
    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            HealthRecordType.HR,
            Instant.parse("2026-05-11T10:00:00Z"),
            (short) 72,
            null,
            null,
            null);

    given(userRepository.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> healthRecordService.saveHealthRecord(1L, command))
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).should().findById(1L);
    then(thresholdRepository).should(never()).findByUserIdAndMetricIn(any(), any());
    then(thresholdEvaluator).should(never()).evaluate(any(), any());
    then(healthRecordRepository).should(never()).save(any());
    then(recordViolationRepository).should(never()).saveAll(any());
    then(alertRepository).should(never()).save(any());
  }

  @DisplayName("HR 타입에 BP 필드가 함께 오면 건강 기록을 저장하지 않는다")
  @Test
  void saveHealthRecordHeartRateTypeMismatch() {
    HealthRecordCreateCommand command =
        new HealthRecordCreateCommand(
            HealthRecordType.HR,
            Instant.parse("2026-05-11T10:00:00Z"),
            (short) 72,
            (short) 120,
            null,
            null);

    assertThatThrownBy(() -> healthRecordService.saveHealthRecord(1L, command))
        .isInstanceOf(HealthRecordTypeMismatchException.class);

    then(userRepository).should(never()).findById(any());
    then(healthRecordRepository).should(never()).save(any());
    then(recordViolationRepository).should(never()).saveAll(any());
    then(alertRepository).should(never()).save(any());
  }
}
