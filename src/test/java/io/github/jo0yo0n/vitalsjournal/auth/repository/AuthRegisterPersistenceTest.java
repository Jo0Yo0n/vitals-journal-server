package io.github.jo0yo0n.vitalsjournal.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jo0yo0n.vitalsjournal.config.JpaConfig;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.time.Instant;
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

  @DisplayName("삭제되지 않은 사용자의 이메일이 존재하면 true를 반환한다")
  @Test
  void existsByEmailAndDeletedAtIsNull() {
    // given
    userRepository.saveAndFlush(User.of("existing-email", "encoded-password", "nickname"));

    // when
    boolean exists = userRepository.existsByEmailAndDeletedAtIsNull("existing-email");

    // then
    assertThat(exists).isTrue();
  }

  @DisplayName("삭제되지 않은 사용자의 닉네임이 존재하면 true를 반환한다")
  @Test
  void existsByNicknameAndDeletedAtIsNull() {
    // given
    userRepository.saveAndFlush(User.of("email", "encoded-password", "existing-nickname"));

    // when
    boolean exists = userRepository.existsByNicknameAndDeletedAtIsNull("existing-nickname");

    // then
    assertThat(exists).isTrue();
  }

  @DisplayName("삭제된 사용자의 이메일은 존재하지 않는 것으로 본다")
  @Test
  void existsByEmailAndDeletedAtIsNullReturnsFalseForDeletedUser() {
    // given
    User deletedUser = User.of("deleted-email", "encoded-password", "nickname");
    deletedUser.delete(Instant.now());
    userRepository.saveAndFlush(deletedUser);

    // when
    boolean exists = userRepository.existsByEmailAndDeletedAtIsNull("deleted-email");

    // then
    assertThat(exists).isFalse();
  }

  @DisplayName("삭제된 사용자의 닉네임은 존재하지 않는 것으로 본다")
  @Test
  void existsByNicknameAndDeletedAtIsNullReturnsFalseForDeletedUser() {
    // given
    User deletedUser = User.of("email", "encoded-password", "deleted-nickname");
    deletedUser.delete(Instant.now());
    userRepository.saveAndFlush(deletedUser);

    // when
    boolean exists = userRepository.existsByNicknameAndDeletedAtIsNull("deleted-nickname");

    // then
    assertThat(exists).isFalse();
  }

  @DisplayName("삭제되지 않은 사용자 이메일을 중복 저장은 DB에서 제약 조건 위반을 발생시킨다")
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

  @DisplayName("삭제되지 않은 사용자 닉네임을 중복 저장은 DB에서 제약 조건 위반을 발생시킨다")
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

  @DisplayName("삭제된 사용자의 이메일과 닉네임은 중복 저장이 가능하다")
  @Test
  void saveDuplicateDeletedUserAllowsDuplicate() {
    // given
    User deletedUser = User.of("deleted-email", "encoded-password", "deleted-nickname");
    deletedUser.delete(Instant.now());
    userRepository.saveAndFlush(deletedUser);

    // when / then
    assertThatCode(
            () ->
                userRepository.saveAndFlush(
                    User.of("deleted-email", "encoded-password", "deleted-nickname")))
        .doesNotThrowAnyException();
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
