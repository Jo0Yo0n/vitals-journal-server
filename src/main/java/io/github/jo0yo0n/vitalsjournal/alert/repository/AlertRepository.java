package io.github.jo0yo0n.vitalsjournal.alert.repository;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface AlertRepository extends Repository<Alert, Long> {

  Alert save(Alert alert);

  List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);
}
