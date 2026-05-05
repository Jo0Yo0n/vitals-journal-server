package io.github.jo0yo0n.vitalsjournal.auth.util;

import io.github.jo0yo0n.vitalsjournal.auth.exception.InvalidTokenSubjectException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtSubjects {

  private JwtSubjects() {}

  public static Long requireUserId(Jwt jwt) {
    String subject = jwt.getSubject();

    if (subject == null || subject.isBlank()) {
      throw new InvalidTokenSubjectException();
    }

    try {
      return Long.parseLong(subject);
    } catch (NumberFormatException e) {
      throw new InvalidTokenSubjectException();
    }
  }
}
