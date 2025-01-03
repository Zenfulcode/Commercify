package com.zenfulcode.commercify.shared.interfaces;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final String status;
    private final T data;
    private final ErrorInfo error;
    private final MetaInfo meta;
    private final Instant timestamp;

    private ApiResponse(String status, T data, ErrorInfo error, MetaInfo meta) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.meta = meta;
        this.timestamp = Instant.now();
    }

    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null, null);
    }

    // Success response with data and metadata
    public static <T> ApiResponse<T> success(T data, MetaInfo meta) {
        return new ApiResponse<>("success", data, null, meta);
    }

    // Error response
    public static <T> ApiResponse<T> error(ErrorInfo error) {
        return new ApiResponse<>("error", null, error, null);
    }

    // Error response with http status
    public static <T> ApiResponse<T> error(String message, String code, int httpStatus) {
        ErrorInfo error = new ErrorInfo(message, code, httpStatus);
        return new ApiResponse<>("error", null, error, null);
    }

    // Validation error response
    public static <T> ApiResponse<T> validationError(List<ValidationError> validationErrors) {
        ErrorInfo error = new ErrorInfo(
                "Validation failed",
                "VALIDATION_ERROR",
                400,
                validationErrors
        );
        return new ApiResponse<>("error", null, error, null);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        private final String message;
        private final String code;
        private final int httpStatus;
        private final List<ValidationError> validationErrors;

        public ErrorInfo(String message, String code, int httpStatus) {
            this(message, code, httpStatus, null);
        }

        public ErrorInfo(String message, String code, int httpStatus,
                         List<ValidationError> validationErrors) {
            this.message = message;
            this.code = code;
            this.httpStatus = httpStatus;
            this.validationErrors = validationErrors;
        }
    }

    public record ValidationError(String field, String message) {
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaInfo {
        private final Integer page;
        private final Integer size;
        private final Long totalElements;
        private final Integer totalPages;
        private final String nextPage;
        private final String previousPage;

        private MetaInfo(Builder builder) {
            this.page = builder.page;
            this.size = builder.size;
            this.totalElements = builder.totalElements;
            this.totalPages = builder.totalPages;
            this.nextPage = builder.nextPage;
            this.previousPage = builder.previousPage;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer page;
            private Integer size;
            private Long totalElements;
            private Integer totalPages;
            private String nextPage;
            private String previousPage;

            public Builder page(Integer page) {
                this.page = page;
                return this;
            }

            public Builder size(Integer size) {
                this.size = size;
                return this;
            }

            public Builder totalElements(Long totalElements) {
                this.totalElements = totalElements;
                return this;
            }

            public Builder totalPages(Integer totalPages) {
                this.totalPages = totalPages;
                return this;
            }

            public Builder nextPage(String nextPage) {
                this.nextPage = nextPage;
                return this;
            }

            public Builder previousPage(String previousPage) {
                this.previousPage = previousPage;
                return this;
            }

            public MetaInfo build() {
                return new MetaInfo(this);
            }
        }
    }
}