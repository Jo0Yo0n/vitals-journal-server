package io.github.jo0yo0n.vitalsjournal.alert.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.repository.HealthRecordRepository;
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
class AlertRepositoryTest {

  @Autowired private AlertRepository alertRepository;
  @Autowired private HealthRecordRepository healthRecordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;

  @DisplayName("사용자별 알림 목록은 다른 사용자의 알림과 섞이지 않는다")
  @Test
  void findByUserIdOrderByCreatedAtDescKeepsUserDataIsolated() {
    // given
    User userA = userRepository.saveAndFlush(User.of("a@example.com", "hashed-password", "UserA"));
    User userB = userRepository.saveAndFlush(User.of("b@example.com", "hashed-password", "UserB"));

    HealthRecord userARecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                userA, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null));
    HealthRecord userBRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                userB, Instant.parse("2026-05-11T11:00:00Z"), (short) 140, null));

    Alert userAAlert = alertRepository.save(Alert.ofRangeViolation(userA, userARecord));
    alertRepository.save(Alert.ofRangeViolation(userB, userBRecord));

    entityManager.flush();
    entityManager.clear();

    // when
    List<Alert> result = alertRepository.findByUserIdOrderByCreatedAtDesc(userA.getId());

    // then
    assertThat(result).extracting(Alert::getId).containsExactly(userAAlert.getId());
  }

  @DisplayName("사용자별 알림 목록은 createdAt 최신순으로 조회된다")
  @Test
  void findByUserIdOrderByCreatedAtDescSortsByCreatedAtDesc() {
    // given
    User user =
        userRepository.saveAndFlush(User.of("sort@example.com", "hashed-password", "SortUser"));

    HealthRecord oldRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                user, Instant.parse("2026-05-10T10:00:00Z"), (short) 130, null));
    HealthRecord newRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                user, Instant.parse("2026-05-11T10:00:00Z"), (short) 140, null));

    Alert oldAlert = alertRepository.save(Alert.ofRangeViolation(user, oldRecord));
    Alert newAlert = alertRepository.save(Alert.ofRangeViolation(user, newRecord));
    entityManager.flush();

    setCreatedAt(oldAlert.getId(), Instant.parse("2026-05-10T10:01:00Z"));
    setCreatedAt(newAlert.getId(), Instant.parse("2026-05-11T10:01:00Z"));
    entityManager.flush();
    entityManager.clear();

    // when
    List<Alert> result = alertRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

    // then
    assertThat(result).extracting(Alert::getId).containsExactly(newAlert.getId(), oldAlert.getId());
  }

  @DisplayName("알림 ID와 사용자 ID가 모두 일치하는 알림만 조회한다")
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
                userA, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null));
    HealthRecord userBRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                userB, Instant.parse("2026-05-11T11:00:00Z"), (short) 140, null));

    Alert userAAlert = alertRepository.save(Alert.ofRangeViolation(userA, userARecord));
    alertRepository.save(Alert.ofRangeViolation(userB, userBRecord));

    entityManager.flush();
    entityManager.clear();

    // when
    Optional<Alert> found = alertRepository.findByIdAndUserId(userAAlert.getId(), userA.getId());
    Optional<Alert> notFound = alertRepository.findByIdAndUserId(userAAlert.getId(), userB.getId());

    // then
    assertThat(found).map(Alert::getId).hasValue(userAAlert.getId());
    assertThat(notFound).isEmpty();
  }

  private void setCreatedAt(Long alertId, Instant createdAt) {
    entityManager
        .createNativeQuery("UPDATE alert SET created_at = :createdAt WHERE id = :alertId")
        .setParameter("createdAt", createdAt)
        .setParameter("alertId", alertId)
        .executeUpdate();
  }
}
