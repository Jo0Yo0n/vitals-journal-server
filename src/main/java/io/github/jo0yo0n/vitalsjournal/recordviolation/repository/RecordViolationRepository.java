package io.github.jo0yo0n.vitalsjournal.recordviolation.repository;

import io.github.jo0yo0n.vitalsjournal.recordviolation.domain.RecordViolation;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface RecordViolationRepository extends Repository<RecordViolation, Long> {

  List<RecordViolation> saveAll(Iterable<RecordViolation> violations);

  List<RecordViolation> findByHealthRecordIdOrderByIdAsc(Long healthRecordId);
}
