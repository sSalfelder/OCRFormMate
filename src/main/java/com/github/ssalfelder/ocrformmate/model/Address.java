package com.github.ssalfelder.ocrformmate.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Ort")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Strasse", nullable = false)
    private String street;

    @Column(name = "Postleitzahl", nullable = false)
    private String postalCode;

    @Column(name = "Ort", nullable = false)
    private String city;

    public Integer getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

