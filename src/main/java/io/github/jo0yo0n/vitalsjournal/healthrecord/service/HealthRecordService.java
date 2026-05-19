package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.alert.repository.AlertRepository;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
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
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthRecordService {

  private final HealthRecordRepository healthRecordRepository;
  private final UserRepository userRepository;
  private final ThresholdRepository thresholdRepository;
  private final RecordViolationRepository recordViolationRepository;
  private final AlertRepository alertRepository;
  private final HealthRecordThresholdEvaluator thresholdEvaluator;

  public HealthRecordService(
      HealthRecordRepository healthRecordRepository,
      UserRepository userRepository,
      ThresholdRepository thresholdRepository,
      RecordViolationRepository recordViolationRepository,
      AlertRepository alertRepository,
      HealthRecordThresholdEvaluator thresholdEvaluator) {

    this.healthRecordRepository = healthRecordRepository;
    this.userRepository = userRepository;
    this.thresholdRepository = thresholdRepository;
    this.recordViolationRepository = recordViolationRepository;
    this.alertRepository = alertRepository;
    this.thresholdEvaluator = thresholdEvaluator;
  }

  @Transactional(readOnly = true)
  public List<HealthRecord> getHealthRecordsByUserId(Long userId) {
    return healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(userId);
  }

  @Transactional
  public HealthRecord saveHealthRecord(Long userId, HealthRecordCreateCommand command) {

    if (command.isHeartRate() && !command.hasOnlyHeartRate()) {
      throw new HealthRecordTypeMismatchException(
          "The field set does not match the health record type");
    }

    if (command.isBloodPressure() && !command.hasOnlyBloodPressure()) {
      throw new HealthRecordTypeMismatchException(
          "The field set does not match the health record type");
    }

    User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());

    HealthRecord healthRecord =
        command.isHeartRate()
            ? HealthRecord.ofHeartRate(user, command.measuredAt(), command.bpm(), command.memo())
            : HealthRecord.ofBloodPressure(
                user,
                command.measuredAt(),
                command.systolic(),
                command.diastolic(),
                command.memo());

    List<ThresholdMetric> metrics = healthRecord.getRecordType().thresholdMetrics();

    List<Threshold> thresholdsByMetric =
        thresholdRepository.findByUserIdAndMetricIn(userId, metrics);

    List<RecordViolation> violations =
        thresholdEvaluator.evaluate(healthRecord, thresholdsByMetric);

    healthRecordRepository.save(healthRecord);

    if (!violations.isEmpty()) {
      recordViolationRepository.saveAll(violations);

      Alert alert = Alert.ofRangeViolation(user, healthRecord);
      alertRepository.save(alert);
    }

    return healthRecord;
  }
}
