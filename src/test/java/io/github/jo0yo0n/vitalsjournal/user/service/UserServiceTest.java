package io.github.jo0yo0n.vitalsjournal.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserServiceTest {

  @Mock private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository);
  }

  @DisplayName("UserRepository가 Optional.of(user)를 반환하면 같은 user를 반환한다")
  @Test
  void findByIdSuccess() {
    User user = User.of("testUser", "hashedPassword", "Test User");
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    User foundUser = userService.findById(1L);
    assertThat(foundUser).isEqualTo(user);
  }

  @DisplayName("UserRepository가 Optional.empty()를 반환하면 UserNotFoundException 발생")
  @Test
  void findByIdNotFound() {
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(1L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("Authenticated user does not exist");
  }
}
