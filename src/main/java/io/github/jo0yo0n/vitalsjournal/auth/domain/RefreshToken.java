package io.github.jo0yo0n.vitalsjournal.auth.domain;

import io.github.jo0yo0n.vitalsjournal.common.domain.CreatedTimeEntity;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_token")
public class RefreshToken extends CreatedTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true, length = 255)
  private String tokenHash;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  protected RefreshToken() {}

  public RefreshToken(User user, String tokenHash, Instant issuedAt, Instant expiresAt) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public void revoke(Instant now) {
    if (!isRevoked()) {
      this.revokedAt = now;
    }
  }
}
