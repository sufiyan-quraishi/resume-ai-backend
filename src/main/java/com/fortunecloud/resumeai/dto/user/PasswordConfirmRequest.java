package com.fortunecloud.resumeai.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * Used to confirm the current password before a destructive action (delete account).
 */
public class PasswordConfirmRequest {

    @NotBlank(message = "Enter your password to confirm")
    private String password;

    public PasswordConfirmRequest() {
    }

    public PasswordConfirmRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}