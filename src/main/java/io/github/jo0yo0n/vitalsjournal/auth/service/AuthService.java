package io.github.jo0yo0n.vitalsjournal.auth.service;

import io.github.jo0yo0n.vitalsjournal.auth.exception.EmailAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.NicknameAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final String USERS_EMAIL_ACTIVE_UNIQUE = "ux_users_email_active";
  private static final String USERS_NICKNAME_ACTIVE_UNIQUE = "ux_users_nickname_active";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public void register(String email, String rawPassword, String nickname) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
      throw new EmailAlreadyExistsException();
    }
    if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
      throw new NicknameAlreadyExistsException();
    }

    String encodedPassword = passwordEncoder.encode(rawPassword);
    User user = new User(email, encodedPassword, nickname);

    try {
      userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException e) {
      String constraintName = extractConstraintName(e);

      if (USERS_EMAIL_ACTIVE_UNIQUE.equals(constraintName)) {
        throw new EmailAlreadyExistsException();
      }
      if (USERS_NICKNAME_ACTIVE_UNIQUE.equals(constraintName)) {
        throw new NicknameAlreadyExistsException();
      }

      throw e;
    }
  }

  private String extractConstraintName(Throwable e) {
    Throwable current = e;

    while (current != null) {
      if (current instanceof ConstraintViolationException constraintViolationException) {
        String constraintName = constraintViolationException.getConstraintName();

        if (constraintName != null) {
          return constraintName;
        }
      }

      current = current.getCause();
    }

    return null;
  }
}
