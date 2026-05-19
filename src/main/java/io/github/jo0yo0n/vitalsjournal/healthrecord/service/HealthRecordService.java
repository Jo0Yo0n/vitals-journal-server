package io.github.jo0yo0n.vitalsjournal.healthrecord.service;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import io.github.jo0yo0n.vitalsjournal.healthrecord.repository.HealthRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthRecordService {

  private final HealthRecordRepository healthRecordRepository;

  public HealthRecordService(HealthRecordRepository healthRecordRepository) {
    this.healthRecordRepository = healthRecordRepository;
  }

  @Transactional(readOnly = true)
  public List<HealthRecord> getHealthRecordsByUserId(Long userId) {
    return healthRecordRepository.findByUserIdOrderByMeasuredAtDesc(userId);
  }
}
