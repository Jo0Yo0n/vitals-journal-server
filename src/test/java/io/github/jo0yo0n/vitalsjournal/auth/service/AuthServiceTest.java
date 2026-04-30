package io.github.jo0yo0n.vitalsjournal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.jo0yo0n.vitalsjournal.auth.exception.EmailAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.InvalidCredentialsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.NicknameAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.config.TokenProperties;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.time.Duration;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  private static PasswordEncoder passwordEncoder;
  @Mock private JwtEncoder jwtEncoder;
  @Mock private TokenProperties tokenProperties;

  private AuthService authService;

  @BeforeAll
  static void setUpEncoder() {
    passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordEncoder, jwtEncoder, tokenProperties);
  }

  @DisplayName("이미 존재하는 email로 회원가입하면 EmailAlreadyExistsException 발생")
  @Test
  void registerEmailAlreadyExists() {
    given(userRepository.existsByEmail("existing-email")).willReturn(true);

    assertThatThrownBy(() -> authService.register("existing-email", "password", "nickname"))
        .isInstanceOf(EmailAlreadyExistsException.class);

    then(userRepository).should(never()).existsByNickname(anyString());
    then(userRepository).should(never()).saveAndFlush(any());
  }

  @DisplayName("이미 존재하는 nickname으로 회원가입하면 NicknameAlreadyExistsException 발생")
  @Test
  void registerNicknameAlreadyExists() {
    given(userRepository.existsByEmail("email")).willReturn(false);
    given(userRepository.existsByNickname("existing-nickname")).willReturn(true);

    assertThatThrownBy(() -> authService.register("email", "password", "existing-nickname"))
        .isInstanceOf(NicknameAlreadyExistsException.class);

    then(userRepository).should(never()).saveAndFlush(any());
  }

  @DisplayName("회원가입이 성공하면 비밀번호를 인코딩해서 저장한다")
  @Test
  void registerSavesUserWithEncodedPassword() {
    given(userRepository.existsByEmail("email")).willReturn(false);
    given(userRepository.existsByNickname("nickname")).willReturn(false);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    authService.register("email", "password", "nickname");

    then(userRepository).should().saveAndFlush(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo("email");
    assertThat(savedUser.matchesPassword("password", passwordEncoder)).isTrue();
    assertThat(savedUser.getNickname()).isEqualTo("nickname");
  }

  @DisplayName("저장 중 이메일 unique constraint 위반이 발생하면 EmailAlreadyExistsException으로 변환")
  @Test
  void registerConvertsEmailUniqueConstraintViolation() {
    given(userRepository.existsByEmail("email")).willReturn(false);
    given(userRepository.existsByNickname("nickname")).willReturn(false);

    DataIntegrityViolationException duplicateEmailException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("duplicate", null, "ux_users_email"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(duplicateEmailException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }

  @DisplayName("저장 중 닉네임 unique constraint 위반이 발생하면 NicknameAlreadyExistsException으로 변환")
  @Test
  void registerConvertsNicknameUniqueConstraintViolation() {
    given(userRepository.existsByEmail("email")).willReturn(false);
    given(userRepository.existsByNickname("nickname")).willReturn(false);

    DataIntegrityViolationException duplicateNicknameException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("duplicate", null, "ux_users_nickname"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(duplicateNicknameException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isInstanceOf(NicknameAlreadyExistsException.class);
  }

  @DisplayName("알 수 없는 DB 제약 위반은 DataIntegrityViolationException을 던진다")
  @Test
  void registerThrowsDataIntegrityViolation() {
    given(userRepository.existsByEmail("email")).willReturn(false);
    given(userRepository.existsByNickname("nickname")).willReturn(false);

    DataIntegrityViolationException unknownConstraintException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("unknown", null, "unknown_constraint"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(unknownConstraintException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isSameAs(unknownConstraintException);
  }

  @DisplayName("로그인 성공 시 access token 문자열을 반환하고 user_id를 subject로 하는 JWT를 생성한다")
  @Test
  void loginSuccess() {
    // given
    String rawPassword = "password";
    String encodedPassword = passwordEncoder.encode(rawPassword);
    User user = User.of("test@example.com", encodedPassword, "nickname");

    ReflectionTestUtils.setField(user, "id", 1L);

    Jwt jwt = Jwt.withTokenValue("access-token").header("alg", "RS256").subject("1").build();

    given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
    given(tokenProperties.accessTokenTtl()).willReturn(Duration.ofMinutes(30));
    given(jwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwt);

    // when
    String accessToken = authService.login("test@example.com", rawPassword);

    // then
    assertThat(accessToken).isEqualTo("access-token");

    ArgumentCaptor<JwtEncoderParameters> captor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);

    then(jwtEncoder).should().encode(captor.capture());

    JwtEncoderParameters params = captor.getValue();

    assertThat(params.getClaims().getSubject()).isEqualTo("1");
    assertThat(params.getClaims().getIssuedAt()).isNotNull();
    assertThat(params.getClaims().getExpiresAt()).isNotNull();

    Duration ttl =
        Duration.between(params.getClaims().getIssuedAt(), params.getClaims().getExpiresAt());

    assertThat(ttl).isEqualTo(Duration.ofMinutes(30));
  }

  @DisplayName("존재하지 않는 이메일로 로그인하면 InvalidCredentialsException 발생")
  @Test
  void loginEmailNotFound() {
    // given
    given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> authService.login("unknown@example.com", "password"))
        .isInstanceOf(InvalidCredentialsException.class);

    then(jwtEncoder).should(never()).encode(any());
  }

  @DisplayName("비밀번호가 틀리면 InvalidCredentialsException 발생")
  @Test
  void loginInvalidPassword() {
    // given
    User user = User.of("test@example.com", passwordEncoder.encode("correct-password"), "nickname");

    given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

    // when & then
    assertThatThrownBy(() -> authService.login("test@example.com", "wrong-password"))
        .isInstanceOf(InvalidCredentialsException.class);

    then(jwtEncoder).should(never()).encode(any());
  }

  @DisplayName("로그인 시 JWT 생성 중 오류가 발생하면 예외를 전파한다")
  @Test
  void loginJwtEncodingError() {
    // given
    User user = User.of("test@example.com", passwordEncoder.encode("correct-password"), "nickname");
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
    given(tokenProperties.accessTokenTtl()).willReturn(Duration.ofMinutes(30));
    given(jwtEncoder.encode(any(JwtEncoderParameters.class)))
        .willThrow(new RuntimeException("JWT encoding error"));

    // when & then
    assertThatThrownBy(() -> authService.login("test@example.com", "correct-password"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("JWT encoding error");

    then(jwtEncoder).should().encode(any(JwtEncoderParameters.class));
  }
}
