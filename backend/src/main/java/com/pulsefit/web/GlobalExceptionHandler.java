package com.pulsefit.web;

import com.pulsefit.exception.ConflictException;
import com.pulsefit.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  ProblemDetail handleNotFound(NotFoundException exception) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Not found");
    detail.setDetail(exception.getMessage());
    detail.setType(URI.create("https://pulsefit.dev/problems/not-found"));
    return detail;
  }

  @ExceptionHandler({ConflictException.class, IllegalStateException.class})
  ProblemDetail handleConflict(RuntimeException exception) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    detail.setTitle("Conflict");
    detail.setDetail(exception.getMessage());
    detail.setType(URI.create("https://pulsefit.dev/problems/conflict"));
    return detail;
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  ProblemDetail handleValidation(Exception exception) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Validation failed");
    detail.setDetail(exception.getMessage());
    detail.setType(URI.create("https://pulsefit.dev/problems/validation"));
    return detail;
  }

  @ExceptionHandler(AccessDeniedException.class)
  ProblemDetail handleForbidden(AccessDeniedException exception) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    detail.setTitle("Forbidden");
    detail.setDetail(exception.getMessage());
    detail.setType(URI.create("https://pulsefit.dev/problems/forbidden"));
    return detail;
  }
}
