package io.github.jo0yo0n.vitalsjournal.user.repository;

import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);
}
