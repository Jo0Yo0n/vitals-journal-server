package io.github.jo0yo0n.vitalsjournal.user.repository;

import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailAndDeletedAtIsNull(String email);

  boolean existsByEmailAndDeletedAtIsNull(String email);

  boolean existsByNicknameAndDeletedAtIsNull(String nickname);
}
