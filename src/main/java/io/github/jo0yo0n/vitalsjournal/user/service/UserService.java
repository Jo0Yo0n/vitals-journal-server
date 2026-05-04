package io.github.jo0yo0n.vitalsjournal.user.service;

import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User findById(Long id) {
    return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
  }
}
