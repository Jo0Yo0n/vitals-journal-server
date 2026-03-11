package io.github.jo0yo0n.vitalsjournal.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      BusinessException ex, HttpServletRequest request) {
    String traceId = getOrCreateTraceId(request);
    ErrorCode errorCode = ex.getErrorCode();

    ErrorResponse body = ErrorResponse.of(errorCode, ex.getMessage(), List.of(), traceId);

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String traceId = getOrCreateTraceId(request);
    List<FieldErrorDetail> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new FieldErrorDetail(
                        error.getField(), FieldErrorReasonMapper.from(error.getCode())))
            .toList();

    ErrorResponse body =
        ErrorResponse.of(
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
            fieldErrors,
            traceId);

    return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus()).body(body);
  }

  private String getOrCreateTraceId(HttpServletRequest request) {
    Object existingTraceId = request.getAttribute("traceId");
    if (existingTraceId != null) {
      return existingTraceId.toString();
    }

    String newTraceId = UUID.randomUUID().toString().replace("-", "");
    request.setAttribute("traceId", newTraceId);
    return newTraceId;
  }

  private String getOrCreateTraceId(WebRequest request) {
    Object existingTraceId = request.getAttribute("traceId", WebRequest.SCOPE_REQUEST);
    if (existingTraceId != null) {
      return existingTraceId.toString();
    }

    String newTraceId = UUID.randomUUID().toString().replace("-", "");
    request.setAttribute("traceId", newTraceId, WebRequest.SCOPE_REQUEST);
    return newTraceId;
  }
}
