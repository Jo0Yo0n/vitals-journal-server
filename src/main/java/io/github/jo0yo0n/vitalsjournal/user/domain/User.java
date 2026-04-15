package io.github.jo0yo0n.vitalsjournal.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  private String email;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected User() {}

  public User(String email, String hashedPassword, String nickname, Instant createdAt) {
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.nickname = nickname;
    this.createdAt = createdAt;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete(Instant now) {
    if (!isDeleted()) {
      this.deletedAt = now;
    }
  }
}
