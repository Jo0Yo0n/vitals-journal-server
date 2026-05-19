package io.github.jo0yo0n.vitalsjournal.threshold.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class ThresholdRepositoryTest {

  @Autowired private ThresholdRepository thresholdRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;

  @DisplayName("사용자별 threshold 목록은 다른 사용자의 threshold와 섞이지 않는다")
  @Test
  void findByUserIdKeepsUserDataIsolated() {
    // given
    User userA = userRepository.saveAndFlush(User.of("a@example.com", "hashed-password", "UserA"));
    User userB = userRepository.saveAndFlush(User.of("b@example.com", "hashed-password", "UserB"));

    Threshold userAHr =
        thresholdRepository.save(Threshold.of(userA, ThresholdMetric.HR, null, (short) 120));
    thresholdRepository.save(Threshold.of(userB, ThresholdMetric.HR, null, (short) 140));

    entityManager.flush();
    entityManager.clear();

    // when
    List<Threshold> result = thresholdRepository.findByUserId(userA.getId());

    // then
    assertThat(result).extracting(Threshold::getId).containsExactly(userAHr.getId());
  }

  @DisplayName("같은 metric이라도 userId가 다르면 서로 다른 threshold로 조회된다")
  @Test
  void findByUserIdAndMetricKeepsUserDataIsolated() {
    // given
    User userA =
        userRepository.saveAndFlush(User.of("a2@example.com", "hashed-password", "UserA2"));
    User userB =
        userRepository.saveAndFlush(User.of("b2@example.com", "hashed-password", "UserB2"));

    Threshold userAHr =
        thresholdRepository.save(Threshold.of(userA, ThresholdMetric.HR, null, (short) 120));
    thresholdRepository.save(Threshold.of(userB, ThresholdMetric.HR, null, (short) 140));

    entityManager.flush();
    entityManager.clear();

    // when
    Threshold result =
        thresholdRepository.findByUserIdAndMetric(userA.getId(), ThresholdMetric.HR).orElseThrow();

    // then
    assertThat(result.getId()).isEqualTo(userAHr.getId());
    assertThat(result.getMaxValue()).isEqualTo((short) 120);
  }

  @DisplayName("같은 사용자에게 같은 metric을 중복 저장하면 DB 제약 조건 위반이 발생한다")
  @Test
  void duplicateUserMetricThrowsException() {
    // given
    User user =
        userRepository.saveAndFlush(User.of("dup@example.com", "hashed-password", "DupUser"));

    thresholdRepository.save(Threshold.of(user, ThresholdMetric.HR, null, (short) 120));
    entityManager.flush();

    assertThatThrownBy(
            () -> {
              thresholdRepository.save(Threshold.of(user, ThresholdMetric.HR, null, (short) 130));
              entityManager.flush();
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
