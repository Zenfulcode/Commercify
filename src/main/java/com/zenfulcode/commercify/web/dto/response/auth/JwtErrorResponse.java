package com.zenfulcode.commercify.web.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JwtErrorResponse {
    private int status;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String error;

    public JwtErrorResponse(String message, String error) {
        this.status = 401;
        this.message = message;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
}