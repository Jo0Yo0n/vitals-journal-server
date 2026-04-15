package io.github.jo0yo0n.vitalsjournal.auth.repository;

import io.github.jo0yo0n.vitalsjournal.auth.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RefreshTokenRepository extends Repository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);
}
