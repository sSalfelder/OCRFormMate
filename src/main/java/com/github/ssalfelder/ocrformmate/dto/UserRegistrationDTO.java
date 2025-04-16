package com.github.ssalfelder.ocrformmate.dto;

public class UserRegistrationDTO {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    private String houseNumber;
    private String street;
    private String postalCode;
    private String city;


    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }
}
