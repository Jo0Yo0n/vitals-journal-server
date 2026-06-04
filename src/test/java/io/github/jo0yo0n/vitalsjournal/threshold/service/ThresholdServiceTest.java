package io.github.jo0yo0n.vitalsjournal.threshold.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.threshold.repository.ThresholdRepository;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ThresholdServiceTest {

  @Mock private ThresholdRepository thresholdRepository;
  @Mock private UserRepository userRepository;

  private ThresholdService thresholdService;

  @BeforeEach
  void setUp() {
    thresholdService = new ThresholdService(thresholdRepository, userRepository);
  }

  @DisplayName("사용자 ID로 threshold 목록을 조회한다")
  @Test
  void getThresholdsByUserId() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, (short) 60, (short) 100);

    given(thresholdRepository.findByUserId(1L)).willReturn(List.of(threshold));

    List<Threshold> result = thresholdService.getThresholdsByUserId(1L);

    assertThat(result).containsExactly(threshold);
    then(thresholdRepository).should().findByUserId(1L);
  }

  @DisplayName("기존 threshold가 있으면 range만 수정하고 save하지 않는다")
  @Test
  void upsertThresholdUpdatesExistingThreshold() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    Threshold existingThreshold = Threshold.of(user, ThresholdMetric.HR, (short) 60, (short) 100);

    given(userRepository.findByIdForThresholdUpsert(1L)).willReturn(Optional.of(user));
    given(thresholdRepository.findByUserIdAndMetric(1L, ThresholdMetric.HR))
        .willReturn(Optional.of(existingThreshold));

    Threshold result =
        thresholdService.upsertThreshold(1L, ThresholdMetric.HR, (short) 70, (short) 110);

    assertThat(result).isSameAs(existingThreshold);
    assertThat(existingThreshold.getMinValue()).isEqualTo((short) 70);
    assertThat(existingThreshold.getMaxValue()).isEqualTo((short) 110);

    then(userRepository).should().findByIdForThresholdUpsert(1L);
    then(thresholdRepository).should(never()).save(any());
  }

  @DisplayName("기존 threshold가 없으면 새로 생성해서 저장한다")
  @Test
  void upsertThresholdCreatesNewThreshold() {
    User user = User.of("test@example.com", "hashed-password", "TestUser");
    Threshold newThreshold = Threshold.of(user, ThresholdMetric.HR, (short) 70, (short) 110);

    given(userRepository.findByIdForThresholdUpsert(1L)).willReturn(Optional.of(user));
    given(thresholdRepository.findByUserIdAndMetric(1L, ThresholdMetric.HR))
        .willReturn(Optional.empty());
    given(thresholdRepository.save(any(Threshold.class))).willReturn(newThreshold);

    Threshold result =
        thresholdService.upsertThreshold(1L, ThresholdMetric.HR, (short) 70, (short) 110);

    assertThat(result).isSameAs(newThreshold);
    then(userRepository).should().findByIdForThresholdUpsert(1L);
    then(thresholdRepository).should().save(any(Threshold.class));
  }

  @DisplayName("기존 threshold가 없고 사용자도 없으면 UserNotFoundException이 발생한다")
  @Test
  void upsertThresholdUserNotFound() {
    given(userRepository.findByIdForThresholdUpsert(1L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> thresholdService.upsertThreshold(1L, ThresholdMetric.HR, (short) 70, (short) 110))
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).should().findByIdForThresholdUpsert(1L);
    then(thresholdRepository).shouldHaveNoInteractions();
    then(thresholdRepository).should(never()).save(any());
  }
}
