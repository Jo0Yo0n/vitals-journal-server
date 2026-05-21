package io.github.jo0yo0n.vitalsjournal.alert.repository;

import io.github.jo0yo0n.vitalsjournal.alert.domain.Alert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends Repository<Alert, Long> {

  Alert save(Alert alert);

  @Query(
      """
      select a
      from Alert a
      join fetch a.healthRecord
      where a.user.id = :userId
      order by a.createdAt desc
      """)
  List<Alert> findByUser_IdOrderByCreatedAtDesc(@Param("userId") Long userId);

  @Query(
      """
      select a
      from Alert a
      join fetch a.healthRecord
      where a.id = :alertId and a.user.id = :userId
      """)
  Optional<Alert> findByIdAndUser_Id(@Param("alertId") Long alertId, @Param("userId") Long userId);
}
