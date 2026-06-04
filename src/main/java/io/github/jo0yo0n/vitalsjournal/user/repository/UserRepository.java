package io.github.jo0yo0n.vitalsjournal.user.repository;

import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends Repository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.id = :id")
  Optional<User> findByIdForThresholdUpsert(@Param("id") Long id);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  User saveAndFlush(User user);
}
