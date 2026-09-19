package com.fortunecloud.resumeai.exception;

/**
 * Raised when no AI API key is configured. The service layer catches this and
 * falls back to offline template generation, so the app always returns output.
 */
public class AiNotConfiguredException extends RuntimeException {
    public AiNotConfiguredException(String message) {
        super(message);
    }
}
