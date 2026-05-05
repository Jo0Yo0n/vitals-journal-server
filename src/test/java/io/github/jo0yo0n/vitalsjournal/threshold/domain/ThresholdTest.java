package io.github.jo0yo0n.vitalsjournal.threshold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jo0yo0n.vitalsjournal.threshold.exception.InvalidThresholdRangeException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThresholdTest {

  private final User user = User.of("test@example.com", "hashed-password", "TestUser");

  @DisplayName("minValue와 maxValue가 모두 null이면 예외가 발생한다")
  @Test
  void createWithoutBoundsThrowsException() {
    assertThatThrownBy(() -> Threshold.of(user, ThresholdMetric.HR, null, null))
        .isInstanceOf(InvalidThresholdRangeException.class);
  }

  @DisplayName("minValue가 maxValue보다 크면 예외가 발생한다")
  @Test
  void createWithInvalidRangeThrowsException() {
    assertThatThrownBy(
            () ->
                Threshold.of(user, ThresholdMetric.HR, new BigDecimal("100"), new BigDecimal("60")))
        .isInstanceOf(InvalidThresholdRangeException.class);
  }

  @DisplayName("minValue만 있어도 생성할 수 있다")
  @Test
  void createWithOnlyMinValue() {
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, new BigDecimal("60"), null);

    assertThat(threshold.getMetric()).isEqualTo(ThresholdMetric.HR);
    assertThat(threshold.getMinValue()).isEqualByComparingTo(new BigDecimal("60"));
    assertThat(threshold.getMaxValue()).isNull();
  }

  @DisplayName("maxValue만 있어도 생성할 수 있다")
  @Test
  void createWithOnlyMaxValue() {
    Threshold threshold = Threshold.of(user, ThresholdMetric.HR, null, new BigDecimal("100"));

    assertThat(threshold.getMetric()).isEqualTo(ThresholdMetric.HR);
    assertThat(threshold.getMinValue()).isNull();
    assertThat(threshold.getMaxValue()).isEqualByComparingTo(new BigDecimal("100"));
  }

  @DisplayName("잘못된 range로 수정하면 예외가 발생하고 기존 값은 유지된다")
  @Test
  void updateWithInvalidRangeThrowsException() {
    Threshold threshold =
        Threshold.of(user, ThresholdMetric.HR, new BigDecimal("60"), new BigDecimal("100"));

    assertThatThrownBy(() -> threshold.updateRange(new BigDecimal("120"), new BigDecimal("80")))
        .isInstanceOf(InvalidThresholdRangeException.class);

    assertThat(threshold.getMinValue()).isEqualByComparingTo(new BigDecimal("60"));
    assertThat(threshold.getMaxValue()).isEqualByComparingTo(new BigDecimal("100"));
  }
}
