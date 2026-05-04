package io.github.jo0yo0n.vitalsjournal.user.controller;

import io.github.jo0yo0n.vitalsjournal.auth.exception.InvalidTokenSubjectException;
import io.github.jo0yo0n.vitalsjournal.user.controller.response.UserMeResponse;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public UserMeResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
    String subject = jwt.getSubject();

    if (subject == null || subject.isBlank()) {
      throw new InvalidTokenSubjectException();
    }

    Long userId;
    try {
      userId = Long.parseLong(subject);
    } catch (NumberFormatException e) {
      throw new InvalidTokenSubjectException();
    }

    User user = userService.findById(userId);
    return UserMeResponse.of(userId, user.getEmail(), user.getNickname(), user.getCreatedAt());
  }
}
