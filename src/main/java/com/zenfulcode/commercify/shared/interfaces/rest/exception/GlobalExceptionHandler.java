package com.zenfulcode.commercify.shared.interfaces.rest.exception;

import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import com.zenfulcode.commercify.shared.domain.exception.DomainForbiddenException;
import com.zenfulcode.commercify.shared.domain.exception.DomainValidationException;
import com.zenfulcode.commercify.shared.domain.exception.EntityNotFoundException;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                "DOMAIN_ERROR",
                400
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            DomainValidationException ex) {
        List<ApiResponse.ValidationError> validationErrors = ex.getViolations()
                .stream()
                .map(error ->
                        new ApiResponse.ValidationError(
                                error,
                                "VALIDATION_ERROR"
                        )
                )
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.validationError(validationErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            EntityNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                "NOT_FOUND",
                404
        );

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(DomainForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedOrderCreation(DomainForbiddenException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                "UNAUTHORIZED_ORDER_CREATION",
                403
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
