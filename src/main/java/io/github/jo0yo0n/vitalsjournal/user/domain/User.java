package io.github.jo0yo0n.vitalsjournal.user.domain;

import io.github.jo0yo0n.vitalsjournal.common.domain.CreatedTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User extends CreatedTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  private String email;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  @Column(name = "nickname", nullable = false, length = 50)
  private String nickname;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  protected User() {}

  private User(String email, String hashedPassword, String nickname) {
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.nickname = nickname;
  }

  public static User of(String email, String hashedPassword, String nickname) {
    return new User(email, hashedPassword, nickname);
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete(Instant now) {
    if (!isDeleted()) {
      this.deletedAt = now;
    }
  }

  public String getEmail() {
    return email;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public String getNickname() {
    return nickname;
  }
}
