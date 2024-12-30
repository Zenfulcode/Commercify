package com.zenfulcode.commercify.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private List<String> errors;
}
