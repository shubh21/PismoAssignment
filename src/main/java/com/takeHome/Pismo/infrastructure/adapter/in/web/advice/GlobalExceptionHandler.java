package com.takeHome.Pismo.infrastructure.adapter.in.web.advice;

import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import com.takeHome.Pismo.core.exception.AccountNotFoundException;
import com.takeHome.Pismo.core.exception.DuplicateDocumentNumberException;
import com.takeHome.Pismo.core.exception.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail baseProblem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        return baseProblem(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    private String formatFieldError(FieldError fe) {
        return "%s %s".formatted(fe.getField(), fe.getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

        String detail = ex.getConstraintViolations().stream()
                .map(cv -> "%s %s".formatted(cv.getPropertyPath(), cv.getMessage()))
                .collect(Collectors.joining(", "));

        return baseProblem(HttpStatus.BAD_REQUEST, "Constraint violation", detail, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.BAD_REQUEST, "Malformed JSON request",
                ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage(), request);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.NOT_FOUND, "Account not found", ex.getMessage(), request);
    }

    @ExceptionHandler(exception = {DataAccessException.class, PersistenceException.class})
    public ProblemDetail handleDataAccess(DataAccessException ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Data Persistence Issue", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateDocumentNumberException.class)
    public ProblemDetail handleDuplicateDocumentNumberException(DuplicateDocumentNumberException ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.CONFLICT, "Data Persistence Issue: Duplicate Entry", ex.getMessage(), request);

    }

    @ExceptionHandler(AccountDoesNotExistException.class)
    public ProblemDetail handleAccountDoesNotExistException(AccountDoesNotExistException ex, HttpServletRequest request) {
        return baseProblem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Transaction", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {

        return baseProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error",
                ex.getMessage(),
                request);
    }
}
