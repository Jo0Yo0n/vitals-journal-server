package io.github.jo0yo0n.vitalsjournal.auth.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.auth.exception.EmailAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.exception.NicknameAlreadyExistsException;
import io.github.jo0yo0n.vitalsjournal.auth.service.AuthService;
import io.github.jo0yo0n.vitalsjournal.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @DisplayName("email, password, nickname이 비어 있으면 400 VALIDATION_ERROR")
  @Test
  void registerWithBlankFields() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "",
                      "password": "",
                      "nickname": ""
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[*].name").value(hasItems("email", "password", "nickname")));
  }

  @DisplayName("email 형식이 잘못되면 400 VALIDATION_ERROR")
  @Test
  void registerInvalidEmail() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "invalid-email",
                      "password": "password",
                      "nickname": "nickname"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[*].name").value(hasItems("email")));
  }

  @DisplayName("중복 이메일이면 409 EMAIL_ALREADY_EXISTS")
  @Test
  void registerEmailAlreadyExists() throws Exception {
    doThrow(new EmailAlreadyExistsException())
        .when(authService)
        .register("test@example.com", "password", "nickname");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "password",
                      "nickname": "nickname"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
  }

  @DisplayName("중복 닉네임이면 409 NICKNAME_ALREADY_EXISTS")
  @Test
  void registerNicknameAlreadyExists() throws Exception {
    doThrow(new NicknameAlreadyExistsException())
        .when(authService)
        .register("test@example.com", "password", "existing-nickname");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "password",
                      "nickname": "existing-nickname"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("NICKNAME_ALREADY_EXISTS"));
  }

  @DisplayName("비밀번호가 7자 미만이면 400 VALIDATION_ERROR")
  @Test
  void registerWithShortPassword() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "short",
                      "nickname": "nickname"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors[*].name").value(hasItems("password")));
  }

  @DisplayName("정상 요청은 201 CREATED")
  @Test
  void registerSuccess() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "password": "password",
                      "nickname": "nickname"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(content().string(""));

    then(authService).should().register("test@example.com", "password", "nickname");
  }
}
