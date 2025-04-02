package com.github.ssalfelder.ocrformmate.validation;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidationResult {
    private final List<String> errorMessages = new ArrayList<>();

    public void addError(String message) {
        errorMessages.add(message);
    }

    public boolean isValid() {
        return errorMessages.isEmpty();
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
