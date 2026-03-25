package io.github.jo0yo0n.vitalsjournal.common.error;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
    ProblemDetail problem = newProblemDetail(ex.getErrorCode(), ex.getMessage());
    problem.setType(ex.getErrorCode().type());
    problem.setTitle(ex.getErrorCode().title());

    return handleExceptionInternal(ex, problem, null, ex.getErrorCode().status(), request);
  }

  // DTO 바인딩 시 발생하는 검증 오류를 처리하는 핸들러
  // (예: @RequestBody, @ModelAttribute 검증 실패)
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    ProblemDetail problem =
        newProblemDetail(ErrorCode.VALIDATION_ERROR, "Request validation failed.");
    problem.setProperty("errors", extractBindingErrors(ex.getBindingResult()));

    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  // 메서드 매개변수 검증 시 발생하는 검증 오류를 처리하는 핸들러
  // (예: @RequestParam, @PathVariable 검증 실패)
  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    ErrorCode errorCode =
        ex.isForReturnValue() ? ErrorCode.INTERNAL_SERVER_ERROR : ErrorCode.VALIDATION_ERROR;

    String detail =
        ex.isForReturnValue()
            ? "Method return value validation failed."
            : "Request validation failed.";

    ProblemDetail problem = newProblemDetail(errorCode, detail);
    if (!ex.isForReturnValue()) {
      problem.setProperty("errors", extractMethodValidationErrors(ex));
    }

    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  // HTTP 메시지 변환 오류를 처리하는 핸들러 (예: JSON 파싱 오류)
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    ProblemDetail problem =
        newProblemDetail(ErrorCode.INVALID_REQUEST, "Request body is missing or malformed.");
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  // 요청 파라미터 타입 불일치 오류를 처리하는 핸들러
  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    ProblemDetail problem =
        newProblemDetail(ErrorCode.INVALID_REQUEST, "A request parameter has an invalid type.");

    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  // 그 외 모든 예외를 처리하는 핸들러 (예상치 못한 서버 오류)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnexpectedEntityException(Exception ex, WebRequest request) {
    ProblemDetail problem =
        newProblemDetail(ErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

    return handleExceptionInternal(
        ex, problem, null, ErrorCode.INTERNAL_SERVER_ERROR.status(), request);
  }

  private ProblemDetail newProblemDetail(ErrorCode errorCode, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.status(), detail);
    problemDetail.setType(errorCode.type());
    problemDetail.setTitle(errorCode.title());
    problemDetail.setProperty("errorCode", errorCode.name());
    return problemDetail;
  }

  // BindingResult에서 발생한 검증 오류를 추출하여 InvalidParam 리스트로 변환하는 메서드
  private List<InvalidParam> extractBindingErrors(BindingResult bindingResult) {
    Stream<InvalidParam> fieldErrors =
        bindingResult.getFieldErrors().stream()
            .map(
                fieldError ->
                    new InvalidParam(
                        fieldError.getField(),
                        ValidationErrorReasonMapper.mapping(fieldError.getCode())));

    Stream<InvalidParam> globalErrors =
        bindingResult.getGlobalErrors().stream()
            .map(
                objectError ->
                    new InvalidParam(
                        objectError.getObjectName(),
                        ValidationErrorReasonMapper.mapping(objectError.getCode())));

    return Stream.concat(fieldErrors, globalErrors).toList();
  }

  // HandlerMethodValidationException에서 발생한 검증 오류를 추출하여 InvalidParam 리스트로 변환하는 메서드
  private List<InvalidParam> extractMethodValidationErrors(HandlerMethodValidationException ex) {
    return ex.getParameterValidationResults().stream()
        .flatMap(
            result -> {
              if (result instanceof ParameterErrors errors) {
                Stream<InvalidParam> fieldErrors =
                    errors.getFieldErrors().stream()
                        .map(
                            fieldError ->
                                new InvalidParam(
                                    fieldError.getField(),
                                    ValidationErrorReasonMapper.mapping(fieldError.getCode())));

                Stream<InvalidParam> globalErrors =
                    errors.getGlobalErrors().stream()
                        .map(
                            objectError ->
                                new InvalidParam(
                                    objectError.getObjectName(),
                                    ValidationErrorReasonMapper.mapping(objectError.getCode())));

                return Stream.concat(fieldErrors, globalErrors);
              }
              String parameterName =
                  Optional.ofNullable(result.getMethodParameter().getParameterName())
                      .orElse("parameter");

              return result.getResolvableErrors().stream()
                  .map(
                      error -> {
                        String[] codes = error.getCodes();
                        if (codes == null || codes.length == 0) {
                          return new InvalidParam(parameterName, "invalid");
                        }
                        return new InvalidParam(
                            parameterName,
                            ValidationErrorReasonMapper.mapping(codes[codes.length - 1]));
                      });
            })
        .toList();
  }
}
