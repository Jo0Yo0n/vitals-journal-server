package io.github.jo0yo0n.vitalsjournal.threshold.service;

import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import io.github.jo0yo0n.vitalsjournal.threshold.repository.ThresholdRepository;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThresholdService {

  private final ThresholdRepository thresholdRepository;
  private final UserRepository userRepository;

  public ThresholdService(ThresholdRepository thresholdRepository, UserRepository userRepository) {
    this.thresholdRepository = thresholdRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<Threshold> getThresholdsByUserId(Long userId) {
    return thresholdRepository.findByUserId(userId);
  }

  @Transactional
  public Threshold upsertThreshold(
      Long userId, ThresholdMetric metric, BigDecimal minValue, BigDecimal maxValue) {

    return thresholdRepository
        .findByUserIdAndMetric(userId, metric)
        .map(
            existingThreshold -> {
              existingThreshold.updateRange(minValue, maxValue);
              return existingThreshold;
            })
        .orElseGet(
            () -> {
              User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

              return thresholdRepository.save(Threshold.of(user, metric, minValue, maxValue));
            });
  }
}
