package io.github.jo0yo0n.vitalsjournal.alert.service;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import io.github.jo0yo0n.vitalsjournal.alert.repository.AlertRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

  private final AlertRepository alertRepository;

  public AlertService(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  @Transactional(readOnly = true)
  public List<Alert> getAlerts(Long userId) {
    return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
