package ai.coworkers.localserver.controller;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 도메인 예외를 HTTP 문제 응답으로 바꿈 */
@RestControllerAdvice
class ApiExceptionHandler {

  /**
   * 대상이 없으면 404로 응답함.
   *
   * @param e 예외
   * @return 문제 응답
   */
  @ExceptionHandler(NoSuchElementException.class)
  ProblemDetail notFound(NoSuchElementException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, String.valueOf(e.getMessage()));
  }

  /**
   * 상태가 충돌하면 409로 응답함.
   *
   * @param e 예외
   * @return 문제 응답
   */
  @ExceptionHandler(IllegalStateException.class)
  ProblemDetail conflict(IllegalStateException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, String.valueOf(e.getMessage()));
  }

  /**
   * 인자가 규칙에 맞지 않으면 400으로 응답함.
   *
   * @param e 예외
   * @return 문제 응답
   */
  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail badRequest(IllegalArgumentException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, String.valueOf(e.getMessage()));
  }
}
