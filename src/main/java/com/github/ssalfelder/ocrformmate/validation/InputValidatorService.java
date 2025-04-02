package com.github.ssalfelder.ocrformmate.validation;

import org.springframework.stereotype.Service;

@Service
public class InputValidatorService {

    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }

    public boolean isValidPostalCode(String postalCode) {
        return postalCode != null && postalCode.matches("\\d{5}");
    }

    public boolean isValidCity(String city) {
        return city != null && city.matches("^[A-Za-zÄäÖöÜüß\\-\\.\\s]{2,40}$");
    }

    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[\\d +()-]{7,20}");
    }

    public boolean isValidStreet(String street) {
        return street != null && street.matches("^[A-Za-zÄäÖöÜüß\\\\-\\\\.\\\\s]{2,40}");
    }

    public boolean isValidHouseNumber(String houseNumber) {
        return houseNumber != null && houseNumber.matches("^\\d+[a-zA-Z]?$");
    }

    public boolean isValidFirstname(String firstname) {
        return firstname != null && firstname.matches("^[A-Za-zÄäÖöÜüß\\- ]{2,30}$");
    }

    public boolean isValidLastname(String lastname) {
        return lastname != null && lastname.matches("^[A-Za-zÄäÖöÜüß\\- ]{2,30}$");
    }
}

