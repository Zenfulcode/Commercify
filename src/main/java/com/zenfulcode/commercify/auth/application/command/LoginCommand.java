package com.zenfulcode.commercify.auth.application.command;

public record LoginCommand(String email, String password, boolean isGuest) {
}
