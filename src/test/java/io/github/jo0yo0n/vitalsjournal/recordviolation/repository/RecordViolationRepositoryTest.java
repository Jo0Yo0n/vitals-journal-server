package io.github.jo0yo0n.vitalsjournal.recordviolation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.repository.HealthRecordRepository;
import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class RecordViolationRepositoryTest {

  @Autowired private RecordViolationRepository recordViolationRepository;
  @Autowired private HealthRecordRepository healthRecordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;

  @DisplayName("건강 기록 ID로 해당 기록의 위반 내역만 ID 오름차순으로 조회한다")
  @Test
  void findByHealthRecordIdOrderByIdAsc() {
    User user =
        userRepository.saveAndFlush(User.of("violation@example.com", "hashed-password", "User"));
    HealthRecord targetRecord =
        healthRecordRepository.save(
            HealthRecord.ofBloodPressure(
                user, Instant.parse("2026-05-11T10:00:00Z"), (short) 140, (short) 95, null));
    HealthRecord otherRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                user, Instant.parse("2026-05-11T11:00:00Z"), (short) 130, null));

    RecordViolation targetSystolicViolation =
        RecordViolation.ofAboveMax(targetRecord, ThresholdMetric.BP_SYS, (short) 140, (short) 130);
    RecordViolation targetDiastolicViolation =
        RecordViolation.ofAboveMax(targetRecord, ThresholdMetric.BP_DIA, (short) 95, (short) 90);
    RecordViolation otherViolation =
        RecordViolation.ofAboveMax(otherRecord, ThresholdMetric.HR, (short) 130, (short) 120);
    recordViolationRepository.saveAll(
        List.of(targetSystolicViolation, otherViolation, targetDiastolicViolation));

    entityManager.flush();
    entityManager.clear();

    List<RecordViolation> result =
        recordViolationRepository.findByHealthRecordIdOrderByIdAsc(targetRecord.getId());

    assertThat(result)
        .extracting(RecordViolation::getMetric)
        .containsExactly(ThresholdMetric.BP_SYS, ThresholdMetric.BP_DIA);
  }
}
