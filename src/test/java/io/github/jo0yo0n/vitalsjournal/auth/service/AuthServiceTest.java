package io.github.jo0yo0n.vitalsjournal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.github.jo0yo0n.vitalsjournal.auth.exception.EmailAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.NicknameAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  private static PasswordEncoder passwordEncoder;

  private AuthService authService;

  @BeforeAll
  static void setUpEncoder() {
    passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordEncoder);
  }

  @DisplayName("이미 존재하는 email로 회원가입하면 EmailAlreadyExistsException 발생")
  @Test
  void registerEmailAlreadyExists() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("existing-email")).willReturn(true);

    assertThatThrownBy(() -> authService.register("existing-email", "password", "nickname"))
        .isInstanceOf(EmailAlreadyExistsException.class);

    then(userRepository).should(never()).existsByNicknameAndDeletedAtIsNull(anyString());
    then(userRepository).should(never()).saveAndFlush(any());
  }

  @DisplayName("이미 존재하는 nickname으로 회원가입하면 NicknameAlreadyExistsException 발생")
  @Test
  void registerNicknameAlreadyExists() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("email")).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull("existing-nickname")).willReturn(true);

    assertThatThrownBy(() -> authService.register("email", "password", "existing-nickname"))
        .isInstanceOf(NicknameAlreadyExistsException.class);

    then(userRepository).should(never()).saveAndFlush(any());
  }

  @DisplayName("회원가입이 성공하면 비밀번호를 인코딩해서 저장한다")
  @Test
  void registerSavesUserWithEncodedPassword() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("email")).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull("nickname")).willReturn(false);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    authService.register("email", "password", "nickname");

    then(userRepository).should().saveAndFlush(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo("email");
    assertThat(passwordEncoder.matches("password", savedUser.getHashedPassword())).isTrue();
    assertThat(savedUser.getNickname()).isEqualTo("nickname");
  }

  @DisplayName("저장 중 이메일 unique constraint 위반이 발생하면 EmailAlreadyExistsException으로 변환")
  @Test
  void registerConvertsEmailUniqueConstraintViolation() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("email")).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull("nickname")).willReturn(false);

    DataIntegrityViolationException duplicateEmailException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("duplicate", null, "ux_users_email_active"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(duplicateEmailException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }

  @DisplayName("저장 중 닉네임 unique constraint 위반이 발생하면 NicknameAlreadyExistsException으로 변환")
  @Test
  void registerConvertsNicknameUniqueConstraintViolation() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("email")).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull("nickname")).willReturn(false);

    DataIntegrityViolationException duplicateNicknameException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("duplicate", null, "ux_users_nickname_active"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(duplicateNicknameException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isInstanceOf(NicknameAlreadyExistsException.class);
  }

  @DisplayName("알 수 없는 DB 제약 위반은 DataIntegrityViolationException을 던진다")
  @Test
  void registerThrowsDataIntegrityViolation() {
    given(userRepository.existsByEmailAndDeletedAtIsNull("email")).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull("nickname")).willReturn(false);

    DataIntegrityViolationException unknownConstraintException =
        new DataIntegrityViolationException(
            "could not execute statement",
            new ConstraintViolationException("unknown", null, "unknown_constraint"));

    given(userRepository.saveAndFlush(any(User.class))).willThrow(unknownConstraintException);

    assertThatThrownBy(() -> authService.register("email", "password", "nickname"))
        .isSameAs(unknownConstraintException);
  }
}
