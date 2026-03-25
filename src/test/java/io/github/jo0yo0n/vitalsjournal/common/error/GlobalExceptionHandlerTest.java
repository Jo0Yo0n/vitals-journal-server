package io.github.jo0yo0n.vitalsjournal.common.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.jo0yo0n.vitalsjournal.support.ExceptionTestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExceptionTestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @DisplayName("BusinessException은 problem detail 형식으로 반환된다")
  @Test
  void handleBusinessException() throws Exception {
    mockMvc
        .perform(get("/test/business"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("http://localhost:8080/problems/account-deleted"))
        .andExpect(jsonPath("$.title").value("Account deleted"))
        .andExpect(jsonPath("$.detail").value("Account deleted"))
        .andExpect(jsonPath("$.errorCode").value("ACCOUNT_DELETED"))
        .andExpect(jsonPath("$.instance").value("/test/business"))
        .andExpect(jsonPath("$.errors").doesNotExist());
  }

  @DisplayName("@RequestBody 검증 실패는 validation problem detail을 반환한다")
  @Test
  void handleMethodArgumentNotValidException() throws Exception {
    mockMvc
        .perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("http://localhost:8080/problems/validation-error"))
        .andExpect(jsonPath("$.title").value("Request validation failed"))
        .andExpect(jsonPath("$.detail").value("Request validation failed."))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.instance").value("/test/body"))
        .andExpect(jsonPath("$.errors[0].name").value("name"))
        .andExpect(jsonPath("$.errors[0].reason").value("required"));
  }

  @DisplayName("@RequestParam 검증 실패는 validation problem detail을 반환한다")
  @Test
  void handleHandlerMethodValidationException() throws Exception {
    mockMvc
        .perform(get("/test/param").param("name", ""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("http://localhost:8080/problems/validation-error"))
        .andExpect(jsonPath("$.title").value("Request validation failed"))
        .andExpect(jsonPath("$.detail").value("Request validation failed."))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.instance").value("/test/param"))
        .andExpect(jsonPath("$.errors[0].name").value("name"))
        .andExpect(jsonPath("$.errors[0].reason").value("required"));
  }

  @DisplayName("경로 변수 타입 불일치는 INVALID_REQUEST를 반환한다")
  @Test
  void handleTypeMismatchException() throws Exception {
    mockMvc
        .perform(get("/test/path/not-a-number"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
  }

  @DisplayName("잘못된 JSON은 invalid request problem detail을 반환한다")
  @Test
  void handleHttpMessageNotReadableException() throws Exception {
    mockMvc
        .perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("http://localhost:8080/problems/invalid-request"))
        .andExpect(jsonPath("$.title").value("Invalid request"))
        .andExpect(jsonPath("$.detail").value("Request body is missing or malformed."))
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.instance").value("/test/body"))
        .andExpect(jsonPath("$.errors").doesNotExist());
  }
}
