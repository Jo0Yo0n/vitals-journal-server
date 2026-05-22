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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@SuppressWarnings("null")
class AlertRepositoryTest {

  @Autowired private AlertRepository alertRepository;
  @Autowired private HealthRecordRepository healthRecordRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;
  @Autowired private PlatformTransactionManager transactionManager;

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

  @DisplayName("읽지 않은 알림만 readAt을 설정하고 이미 읽은 알림은 덮어쓰지 않는다")
  @Test
  void markAsReadIfUnreadUpdatesOnlyUnreadAlert() {
    // given
    User user =
        userRepository.saveAndFlush(User.of("read@example.com", "hashed-password", "ReadUser"));
    HealthRecord healthRecord =
        healthRecordRepository.save(
            HealthRecord.ofHeartRate(
                user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null));
    Alert alert = alertRepository.save(Alert.ofRangeViolation(user, healthRecord));
    entityManager.flush();
    entityManager.clear();

    Instant firstReadAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    Instant secondReadAt = firstReadAt.plusSeconds(1);

    // when
    int firstUpdated = alertRepository.markAsReadIfUnread(alert.getId(), user.getId(), firstReadAt);
    int secondUpdated =
        alertRepository.markAsReadIfUnread(alert.getId(), user.getId(), secondReadAt);
    entityManager.flush();
    entityManager.clear();

    Alert found = alertRepository.findByIdAndUserId(alert.getId(), user.getId()).orElseThrow();

    // then
    assertThat(firstUpdated).isOne();
    assertThat(secondUpdated).isZero();
    assertThat(found.getReadAt()).isEqualTo(firstReadAt);
  }

  @DisplayName("동시 읽음 처리 요청은 readAt을 한 번만 설정한다")
  @Test
  void markAsReadIfUnreadIsAtomicUnderConcurrentRequests() throws Exception {
    String email = "concurrency-alert-" + System.nanoTime() + "@example.com";

    SavedAlert savedAlert = saveAlertInNewTransaction(email);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    Instant firstReadAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    Instant secondReadAt = firstReadAt.plusSeconds(1);

    try {
      Future<Integer> firstUpdate =
          executorService.submit(
              () ->
                  markAsReadAfterStart(
                      ready, start, savedAlert.alertId(), savedAlert.userId(), firstReadAt));
      Future<Integer> secondUpdate =
          executorService.submit(
              () ->
                  markAsReadAfterStart(
                      ready, start, savedAlert.alertId(), savedAlert.userId(), secondReadAt));

      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      start.countDown();

      List<Integer> updateCounts =
          List.of(firstUpdate.get(5, TimeUnit.SECONDS), secondUpdate.get(5, TimeUnit.SECONDS));
      Alert found =
          inNewTransaction(
              () ->
                  alertRepository
                      .findByIdAndUserId(savedAlert.alertId(), savedAlert.userId())
                      .orElseThrow());
      int thirdUpdated =
          inNewTransaction(
              () ->
                  alertRepository.markAsReadIfUnread(
                      savedAlert.alertId(), savedAlert.userId(), firstReadAt.plusSeconds(2)));
      Alert afterThirdUpdate =
          inNewTransaction(
              () ->
                  alertRepository
                      .findByIdAndUserId(savedAlert.alertId(), savedAlert.userId())
                      .orElseThrow());

      assertThat(updateCounts).containsExactlyInAnyOrder(1, 0);
      assertThat(found.getReadAt()).isIn(firstReadAt, secondReadAt);
      assertThat(thirdUpdated).isZero();
      assertThat(afterThirdUpdate.getReadAt()).isEqualTo(found.getReadAt());
    } finally {
      executorService.shutdownNow();
      deleteUserByEmail(email);
    }
  }

  private void setCreatedAt(Long alertId, Instant createdAt) {
    entityManager
        .createNativeQuery("UPDATE alert SET created_at = :createdAt WHERE id = :alertId")
        .setParameter("createdAt", createdAt)
        .setParameter("alertId", alertId)
        .executeUpdate();
  }

  private Integer markAsReadAfterStart(
      CountDownLatch ready, CountDownLatch start, Long alertId, Long userId, Instant readAt)
      throws InterruptedException {

    ready.countDown();
    if (!start.await(5, TimeUnit.SECONDS)) {
      throw new IllegalStateException("Concurrent update start signal was not received");
    }
    return inNewTransaction(() -> alertRepository.markAsReadIfUnread(alertId, userId, readAt));
  }

  private SavedAlert saveAlertInNewTransaction(String email) {
    return inNewTransaction(
        () -> {
          User user = userRepository.saveAndFlush(User.of(email, "hashed-password", "User"));
          HealthRecord healthRecord =
              healthRecordRepository.save(
                  HealthRecord.ofHeartRate(
                      user, Instant.parse("2026-05-11T10:00:00Z"), (short) 130, null));
          Alert alert = alertRepository.save(Alert.ofRangeViolation(user, healthRecord));
          entityManager.flush();
          return new SavedAlert(user.getId(), alert.getId());
        });
  }

  private void deleteUserByEmail(String email) {
    inNewTransaction(
        () -> {
          entityManager
              .createNativeQuery(
                  "DELETE FROM alert WHERE user_id IN (SELECT id FROM users WHERE email = :email)")
              .setParameter("email", email)
              .executeUpdate();
          entityManager
              .createNativeQuery(
                  "DELETE FROM health_record WHERE user_id IN "
                      + "(SELECT id FROM users WHERE email = :email)")
              .setParameter("email", email)
              .executeUpdate();
          entityManager
              .createNativeQuery("DELETE FROM users WHERE email = :email")
              .setParameter("email", email)
              .executeUpdate();
        });
  }

  private <T> T inNewTransaction(Supplier<T> action) {
    return newTransactionTemplate().execute(status -> action.get());
  }

  private void inNewTransaction(Runnable action) {
    newTransactionTemplate()
        .executeWithoutResult(
            status -> {
              action.run();
            });
  }

  private TransactionTemplate newTransactionTemplate() {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return transactionTemplate;
  }

  private record SavedAlert(Long userId, Long alertId) {}
}
