package io.github.jo0yo0n.vitalsjournal.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class AuthRegisterPersistenceTest {

  @Autowired private UserRepository userRepository;

  @DisplayName("이메일이 존재하면 true를 반환한다")
  @Test
  void existsByEmail() {
    // given
    userRepository.saveAndFlush(User.of("existing-email", "encoded-password", "nickname"));

    // when
    boolean exists = userRepository.existsByEmail("existing-email");

    // then
    assertThat(exists).isTrue();
  }

  @DisplayName("닉네임이 존재하면 true를 반환한다")
  @Test
  void existsByNickname() {
    // given
    userRepository.saveAndFlush(User.of("email", "encoded-password", "existing-nickname"));

    // when
    boolean exists = userRepository.existsByNickname("existing-nickname");

    // then
    assertThat(exists).isTrue();
  }

  @DisplayName("이메일을 중복 저장하면 DB에서 제약 조건 위반을 발생시킨다")
  @Test
  void saveDuplicateEmailThrowsException() {
    // given
    userRepository.saveAndFlush(User.of("duplicate-email", "encoded-password", "nickname1"));

    // when / then
    assertThatThrownBy(
            () ->
                userRepository.saveAndFlush(
                    User.of("duplicate-email", "encoded-password", "nickname2")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @DisplayName("닉네임을 중복 저장하면 DB에서 제약 조건 위반을 발생시킨다")
  @Test
  void saveDuplicateNicknameThrowsException() {
    // given
    userRepository.saveAndFlush(User.of("email1", "encoded-password", "duplicate-nickname"));

    // when / then
    assertThatThrownBy(
            () ->
                userRepository.saveAndFlush(
                    User.of("email2", "encoded-password", "duplicate-nickname")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @DisplayName("대소문자만 다른 이메일도 중복으로 간주한다")
  @Test
  void saveDuplicateEmailWithDifferentCaseThrowsException() {
    // given
    userRepository.saveAndFlush(User.of("duplicate-email", "encoded-password", "nickname1"));

    // when / then
    assertThatThrownBy(
            () ->
                userRepository.saveAndFlush(
                    User.of("DUPLICATE-EMAIL", "encoded-password", "nickname2")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
