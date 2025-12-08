package com.takeHome.Pismo.infrastructure.adapter.in.web.advice;

import com.takeHome.Pismo.core.exception.AccountDoesNotExistException;
import com.takeHome.Pismo.core.exception.AccountNotFoundException;
import com.takeHome.Pismo.core.exception.DuplicateDocumentNumberException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_EXISTS_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.ACCOUNT_NOT_FOUND_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG;
import static com.takeHome.Pismo.core.Constants.INVALID_DOCUMENT_NUMBER_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @Mock
    HttpServletRequest httpServletRequest;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    private HttpServletRequest mockRequest(String path) {
        when(httpServletRequest.getRequestURI()).thenReturn(path);
        return httpServletRequest;
    }


    @Test
    void givenMethodArgumentNotValidException_whenHandled_thenReturnsBadRequestProblemDetailWithValidationMessages() {
        // Given
        HttpServletRequest request = mockRequest("/accounts");


        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createAccountRequest");
        bindingResult.addError(new FieldError("createAccountRequest", "documentNumber", "must not be null"));


        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ProblemDetail problem = handler.handleMethodArgumentNotValid(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Validation failed");
        assertThat(problem.getDetail())
                .contains("documentNumber")
                .contains("must not be null");

        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/accounts");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }


    @Test
    void givenConstraintViolationException_whenHandled_thenReturnsBadRequestProblemDetailWithDetails() {
        // Given
        HttpServletRequest request = mockRequest("/transactions");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);

        Path path = mock(Path.class);
        when(path.toString()).thenReturn("accountId");
        when(violation.getPropertyPath()).thenReturn(path);

        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        // When
        ProblemDetail problem = handler.handleConstraintViolation(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Constraint violation");
        assertThat(problem.getDetail())
                .contains("accountId")
                .contains("must not be null");

        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/transactions");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }

    @Test
    void givenHttpMessageNotReadableException_whenHandled_thenReturnsBadRequestWithMalformedJsonTitle() {
        // Given
        HttpServletRequest request = mockRequest("/transactions");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", mock(HttpInputMessage.class));

        // When
        ProblemDetail problem = handler.handleHttpMessageNotReadable(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Malformed JSON request");
        assertThat(problem.getDetail()).contains("Malformed JSON"); // from handler
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/transactions");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }


    @Test
    void givenIllegalArgumentException_whenHandled_thenReturnsBadRequestProblemDetail() {
        // Given
        long documentNumber = 0L;
        HttpServletRequest request = mockRequest("/accounts");
        IllegalArgumentException ex = new IllegalArgumentException(INVALID_DOCUMENT_NUMBER_MSG.formatted(documentNumber));

        // When
        ProblemDetail problem = handler.handleIllegalArgument(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Invalid request");
        assertThat(problem.getDetail()).isEqualTo(INVALID_DOCUMENT_NUMBER_MSG.formatted(documentNumber));
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/accounts");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }


    @Test
    void givenAccountNotFoundException_whenHandled_thenReturnsNotFoundProblemDetailWithErrorCode() {
        // Given
        long accountId = 123L;
        HttpServletRequest request = mockRequest("/accounts/%s".formatted(accountId));
        AccountNotFoundException ex = new AccountNotFoundException(ACCOUNT_NOT_FOUND_EXCEPTION_MSG.formatted(accountId));

        // When
        ProblemDetail problem = handler.handleAccountNotFound(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Account not found");
        assertThat(problem.getDetail()).isEqualTo(ACCOUNT_NOT_FOUND_EXCEPTION_MSG.formatted(accountId));

        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/accounts/%s".formatted(accountId));
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }


    @Test
    void givenDataAccessException_whenHandled_thenReturnsInternalServerErrorWithDatabaseErrorMessage() {
        // Given
        String errorMsg = "Something Went Wrong. Please try later";
        HttpServletRequest request = mockRequest("/accounts");
        DataAccessResourceFailureException ex =
                new DataAccessResourceFailureException(errorMsg);

        // When
        ProblemDetail problem = handler.handleDataAccess(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Data Persistence Issue");
        assertThat(problem.getDetail()).contains(errorMsg);
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/accounts");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }

    @Test
    void givenDuplicateDocumentException_whenHandled_thenReturnsConflictProblemDetailWithDatabaseErrorMessage() {
        // Given
        HttpServletRequest request = mockRequest("/accounts");
        DuplicateDocumentNumberException ex = new DuplicateDocumentNumberException(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(1L));

        // When
        ProblemDetail problem = handler.handleDuplicateDocumentNumberException(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Data Persistence Issue: Duplicate Entry");
        assertThat(problem.getDetail()).contains(DUPLICATE_DOCUMENT_NUMBER_EXCEPTION_MSG.formatted(1L));
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/accounts");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }

    @Test
    void givenAccountDoesNotExistException_whenHandled_thenReturnsUnprocessableEntityWithDatabaseErrorMessage() {
        // Given
        HttpServletRequest request = mockRequest("/transactions");
        AccountDoesNotExistException ex = new AccountDoesNotExistException(ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(1L));

        // When
        ProblemDetail problem = handler.handleAccountDoesNotExistException(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problem.getTitle()).isEqualTo("Invalid Transaction");
        assertThat(problem.getDetail()).contains(ACCOUNT_NOT_EXISTS_EXCEPTION_MSG.formatted(1L));
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/transactions");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }


    @Test
    void givenUnexpectedException_whenHandled_thenReturnsInternalServerErrorWithGenericMessage() {
        // Given
        String errorMsg = "Something gone crazy, will serve you later";
        HttpServletRequest request = mockRequest("/any-path");
        Exception ex = new RuntimeException(errorMsg);

        // When
        ProblemDetail problem = handler.handleGeneric(ex, request);

        // Then
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Unexpected error");
        assertThat(problem.getDetail()).contains(errorMsg);
        assertThat(Objects.requireNonNull(problem.getProperties()).get("path")).isEqualTo("/any-path");
        assertThat(problem.getProperties().get("timestamp")).isInstanceOf(Instant.class);
    }
}
