package com.zenfulcode.commercify.api.product.dto.response;

public record PageInfo(
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious
) {
    public PageInfo(int pageNumber, int pageSize, long totalElements, int totalPages) {
        this(
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                pageNumber == 0,
                pageNumber == totalPages - 1,
                pageNumber < totalPages - 1,
                pageNumber > 0
        );
    }
}
