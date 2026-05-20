package io.github.jo0yo0n.vitalsjournal.auth.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.github.jo0yo0n.vitalsjournal.auth.exception.InvalidTokenSubjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtSubjectsTest {

  @DisplayName("숫자 subject를 사용자 ID로 변환한다")
  @Test
  void requireUserIdSuccess() {
    Jwt jwt = jwtWithSubject("1");

    Long userId = JwtSubjects.requireUserId(jwt);

    assertThat(userId).isEqualTo(1L);
  }

  @DisplayName("subject가 없으면 예외가 발생한다")
  @Test
  void requireUserIdWithoutSubjectThrowsException() {
    Jwt jwt = mock(Jwt.class);
    given(jwt.getSubject()).willReturn(null);

    assertThatThrownBy(() -> JwtSubjects.requireUserId(jwt))
        .isInstanceOf(InvalidTokenSubjectException.class);
  }

  @DisplayName("subject가 blank이면 예외가 발생한다")
  @Test
  void requireUserIdWithBlankSubjectThrowsException() {
    Jwt jwt = jwtWithSubject(" ");

    assertThatThrownBy(() -> JwtSubjects.requireUserId(jwt))
        .isInstanceOf(InvalidTokenSubjectException.class);
  }

  @DisplayName("subject가 숫자가 아니면 예외가 발생한다")
  @Test
  void requireUserIdWithNonNumericSubjectThrowsException() {
    Jwt jwt = jwtWithSubject("invalid");

    assertThatThrownBy(() -> JwtSubjects.requireUserId(jwt))
        .isInstanceOf(InvalidTokenSubjectException.class);
  }

  private Jwt jwtWithSubject(String subject) {
    return Jwt.withTokenValue("access-token").header("alg", "RS256").subject(subject).build();
  }
}
