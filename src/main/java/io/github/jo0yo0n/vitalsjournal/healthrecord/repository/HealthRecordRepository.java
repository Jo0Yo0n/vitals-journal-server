package io.github.jo0yo0n.vitalsjournal.healthrecord.repository;

import io.github.jo0yo0n.vitalsjournal.healthrecord.domain.HealthRecord;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface HealthRecordRepository extends Repository<HealthRecord, Long> {

  HealthRecord save(HealthRecord healthRecord);

  List<HealthRecord> findByUserIdOrderByMeasuredAtDesc(Long userId);
}
