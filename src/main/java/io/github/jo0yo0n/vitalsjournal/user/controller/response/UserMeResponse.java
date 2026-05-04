package io.github.jo0yo0n.vitalsjournal.user.controller.response;

import java.time.Instant;

public record UserMeResponse(Long id, String email, String nickname, Instant createdAt) {

  public static UserMeResponse of(Long id, String email, String nickname, Instant createdAt) {
    return new UserMeResponse(id, email, nickname, createdAt);
  }
}
