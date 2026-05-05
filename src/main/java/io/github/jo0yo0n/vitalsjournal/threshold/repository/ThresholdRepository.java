package io.github.jo0yo0n.vitalsjournal.threshold.repository;

import io.github.jo0yo0n.vitalsjournal.threshold.domain.Threshold;
import io.github.jo0yo0n.vitalsjournal.threshold.domain.ThresholdMetric;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ThresholdRepository extends Repository<Threshold, Long> {

  List<Threshold> findByUserId(Long userId);

  Optional<Threshold> findByUserIdAndMetric(Long userId, ThresholdMetric metric);

  Threshold save(Threshold threshold);
}
