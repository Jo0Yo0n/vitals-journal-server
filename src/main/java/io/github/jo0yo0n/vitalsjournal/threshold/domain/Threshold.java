package io.github.jo0yo0n.vitalsjournal.threshold.domain;

import io.github.jo0yo0n.vitalsjournal.common.domain.CreatedTimeEntity;
import io.github.jo0yo0n.vitalsjournal.threshold.exception.InvalidThresholdRangeException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "threshold")
public class Threshold extends CreatedTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "metric", nullable = false, length = 16)
  private ThresholdMetric metric;

  @Column(name = "min_value", precision = 10, scale = 2)
  private BigDecimal minValue;

  @Column(name = "max_value", precision = 10, scale = 2)
  private BigDecimal maxValue;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public static Threshold of(
      User user, ThresholdMetric metric, BigDecimal minValue, BigDecimal maxValue) {

    validateRange(minValue, maxValue);

    Threshold threshold = new Threshold();
    threshold.user = user;
    threshold.metric = metric;
    threshold.minValue = minValue;
    threshold.maxValue = maxValue;
    return threshold;
  }

  public void updateRange(BigDecimal minValue, BigDecimal maxValue) {
    validateRange(minValue, maxValue);

    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  private static void validateRange(BigDecimal minValue, BigDecimal maxValue) {
    if (minValue == null && maxValue == null) {
      throw new InvalidThresholdRangeException();
    }

    if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
      throw new InvalidThresholdRangeException();
    }
  }

  public Long getId() {
    return id;
  }

  public ThresholdMetric getMetric() {
    return metric;
  }

  public BigDecimal getMinValue() {
    return minValue;
  }

  public BigDecimal getMaxValue() {
    return maxValue;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
