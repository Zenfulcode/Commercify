package com.zenfulcode.commercify.api.auth.dto.request;

import com.zenfulcode.commercify.auth.application.command.LoginCommand;

public record LoginRequest(
        String email,
        String password,
        Boolean isGuest
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password, isGuest != null && isGuest);
    }
}