package com.github.ssalfelder.ocrformmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Benutzer")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Vorname", nullable = false)
    private String firstname;

    @Column(name = "Nachname", nullable = false)
    private String lastname;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Passwort", nullable = false)
    private String password;

    private String secret;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSecret() {
        return secret;
    }

    @JsonIgnore
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
