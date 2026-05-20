package io.github.jo0yo0n.vitalsjournal.healthrecord.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class HealthRecordRepositoryTest {

  @Autowired private HealthRecordRepository healthRecordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;

  @DisplayName("사용자별 건강 기록 목록은 다른 사용자의 기록과 섞이지 않는다")
  @Test
  void findByUserIdOrderByMeasuredAtDescKeepsUserDataIsolated() {
    // given
    User userA = userRepository.saveAndFlush(User.of("a@example.com", "hashed-password", "UserA"));
    User userB = userRepository.saveAndFlush(User.of("b@example.com", "hashed-password", "UserB"));

    HealthRecord userARecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                userA, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, null));
    healthRecordRepository.save(
        HealthRecord.ofHeartRate(userB, Instant.parse("2026-05-11T11:00:00Z"), (short) 80, null));

    entityManager.flush();
    entityManager.clear();

    // when
    List<HealthRecord> result =
        healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(userA.getId());

    // then
    assertThat(result).extracting(HealthRecord::getId).containsExactly(userARecord.getId());
  }

  @DisplayName("사용자별 건강 기록 목록은 measuredAt 최신순으로 조회된다")
  @Test
  void findByUserIdOrderByMeasuredAtDescSortsByMeasuredAtDesc() {
    // given
    User user =
        userRepository.saveAndFlush(User.of("sort@example.com", "hashed-password", "SortUser"));

    HealthRecord oldRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                user, Instant.parse("2026-05-10T10:00:00Z"), (short) 70, null));
    HealthRecord newRecord =
        healthRecordRepository.save(
            HealthRecord.ofBloodPressure(
                user, Instant.parse("2026-05-11T10:00:00Z"), (short) 120, (short) 80, null));

    entityManager.flush();
    entityManager.clear();

    // when
    List<HealthRecord> result =
        healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(user.getId());

    // then
    assertThat(result)
        .extracting(HealthRecord::getId)
        .containsExactly(newRecord.getId(), oldRecord.getId());
  }

  @DisplayName("건강 기록 ID와 사용자 ID가 모두 일치하는 기록만 조회한다")
  @Test
  void findByIdAndUserIdKeepsUserDataIsolated() {
    // given
    User userA =
        userRepository.saveAndFlush(User.of("detail-a@example.com", "hashed-password", "UserA"));
    User userB =
        userRepository.saveAndFlush(User.of("detail-b@example.com", "hashed-password", "UserB"));

    HealthRecord userARecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                userA, Instant.parse("2026-05-11T10:00:00Z"), (short) 72, null));
    healthRecordRepository.save(
        HealthRecord.ofHeartRate(userB, Instant.parse("2026-05-11T11:00:00Z"), (short) 80, null));

    entityManager.flush();
    entityManager.clear();

    // when
    Optional<HealthRecord> found =
        healthRecordRepository.findByIdAndUserId(userARecord.getId(), userA.getId());
    Optional<HealthRecord> notFound =
        healthRecordRepository.findByIdAndUserId(userARecord.getId(), userB.getId());

    // then
    assertThat(found).map(HealthRecord::getId).hasValue(userARecord.getId());
    assertThat(notFound).isEmpty();
  }
}
