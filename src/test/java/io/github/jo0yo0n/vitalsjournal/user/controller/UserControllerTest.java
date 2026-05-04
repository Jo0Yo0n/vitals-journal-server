package io.github.jo0yo0n.vitalsjournal.user.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.common.error.GlobalExceptionHandler;
import io.github.jo0yo0n.vitalsjournal.common.error.ProblemDetailFactory;
import io.github.jo0yo0n.vitalsjournal.config.ProblemAuthenticationEntryPoint;
import io.github.jo0yo0n.vitalsjournal.config.SecurityConfig;
import io.github.jo0yo0n.vitalsjournal.user.domain.User;
import io.github.jo0yo0n.vitalsjournal.user.exception.UserNotFoundException;
import io.github.jo0yo0n.vitalsjournal.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({
  GlobalExceptionHandler.class,
  ProblemDetailFactory.class,
  SecurityConfig.class,
  ProblemAuthenticationEntryPoint.class
})
@SuppressWarnings("null")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtDecoder jwtDecoder;

  @DisplayName("GET /user/me - 성공")
  @Test
  void getCurrentUserSuccess() throws Exception {
    User user = User.of("test@example.com", "encoded-password", "TestUser");
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userService.findById(1L)).willReturn(user);

    mockMvc
        .perform(get("/user/me").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("TestUser"));

    then(userService).should().findById(1L);
  }

  @DisplayName("GET /user/me - 401 malformed subject")
  @Test
  void getCurrentUserMalformedSubject() throws Exception {
    mockMvc
        .perform(get("/user/me").with(jwt().jwt(jwt -> jwt.subject("invalid"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Token subject is missing or malformed"));

    then(userService).should(never()).findById(1L);
  }

  @DisplayName("GET /user/me - 401 missing subject")
  @Test
  void getCurrentUserMissingSubject() throws Exception {
    mockMvc
        .perform(
            get("/user/me")
                .with(jwt().jwt(jwt -> jwt.claims(claims -> claims.remove(JwtClaimNames.SUB)))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Token subject is missing or malformed"));

    then(userService).should(never()).findById(1L);
  }

  @DisplayName("GET /user/me - 401 unauthorized")
  @Test
  void getCurrentUserUnauthorized() throws Exception {
    mockMvc
        .perform(get("/user/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Authentication is required"));

    then(userService).should(never()).findById(1L);
  }

  @DisplayName("GET /user/me - 404 user not found")
  @Test
  void getCurrentUserNotFound() throws Exception {
    given(userService.findById(1L)).willThrow(new UserNotFoundException());

    mockMvc
        .perform(get("/user/me").with(jwt().jwt(jwt -> jwt.subject("1"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Authenticated user does not exist"))
        .andExpect(jsonPath("$.instance").value("/user/me"));

    then(userService).should().findById(1L);
  }
}
