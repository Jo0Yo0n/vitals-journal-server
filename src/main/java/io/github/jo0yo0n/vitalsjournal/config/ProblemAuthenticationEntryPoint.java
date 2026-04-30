package io.github.jo0yo0n.vitalsjournal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jo0yo0n.vitalsjournal.common.error.ErrorCode;
import io.github.jo0yo0n.vitalsjournal.common.error.ProblemDetailFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;
  private final ProblemDetailFactory problemDetailFactory;
  private final BearerTokenAuthenticationEntryPoint delegate =
      new BearerTokenAuthenticationEntryPoint();

  public ProblemAuthenticationEntryPoint(
      ObjectMapper objectMapper, ProblemDetailFactory problemDetailFactory) {
    this.objectMapper = objectMapper;
    this.problemDetailFactory = problemDetailFactory;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    delegate.commence(request, response, authException);

    ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

    ProblemDetail problemDetail =
        problemDetailFactory.create(
            errorCode, "Authentication is required", URI.create(request.getRequestURI()));

    response.setStatus(errorCode.status().value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problemDetail);
  }
}
