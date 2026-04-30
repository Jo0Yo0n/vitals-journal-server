package io.github.jo0yo0n.vitalsjournal.auth.service;

import io.github.jo0yo0n.vitalsjournal.auth.exception.EmailAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.InvalidCredentialsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.NicknameAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.config.TokenProperties;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import java.time.Instant;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final String USERS_EMAIL_UNIQUE = "ux_users_email";
  private static final String USERS_NICKNAME_UNIQUE = "ux_users_nickname";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final TokenProperties tokenProperties;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      TokenProperties tokenProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.tokenProperties = tokenProperties;
  }

  @Transactional
  public void register(String email, String rawPassword, String nickname) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException();
    }
    if (userRepository.existsByNickname(nickname)) {
      throw new NicknameAlreadyExistsException();
    }

    String encodedPassword = passwordEncoder.encode(rawPassword);
    User user = User.of(email, encodedPassword, nickname);

    try {
      userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException e) {
      String constraintName = extractConstraintName(e);

      if (USERS_EMAIL_UNIQUE.equals(constraintName)) {
        throw new EmailAlreadyExistsException();
      }
      if (USERS_NICKNAME_UNIQUE.equals(constraintName)) {
        throw new NicknameAlreadyExistsException();
      }

      throw e;
    }
  }

  @Transactional
  public String login(String email, String rawPassword) {

    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException());

    if (!user.matchesPassword(rawPassword, passwordEncoder)) {
      throw new InvalidCredentialsException();
    }

    Instant now = Instant.now();
    Instant expiresAt = now.plus(tokenProperties.accessTokenTtl());

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
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
