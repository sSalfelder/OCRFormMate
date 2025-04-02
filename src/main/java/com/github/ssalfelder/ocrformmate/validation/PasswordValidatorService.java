package com.github.ssalfelder.ocrformmate.validation;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordValidatorService {

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:'\",.<>/?";

    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.length() < 8) {
            result.addError("Mindestens 8 Zeichen erforderlich.");
        }

        if (!password.matches(".*[A-Z].*")) {
            result.addError("Mindestens ein Großbuchstabe erforderlich.");
        }

        if (!password.matches(".*[a-z].*")) {
            result.addError("Mindestens ein Kleinbuchstabe erforderlich.");
        }

        if (!password.matches(".*\\d.*")) {
            result.addError("Mindestens eine Ziffer erforderlich.");
        }

        if (!password.matches(".*[" + Pattern.quote(SPECIAL_CHARS) + "].*")) {
            result.addError("Mindestens ein Sonderzeichen erforderlich.");
        }

        return result;
    }
}

