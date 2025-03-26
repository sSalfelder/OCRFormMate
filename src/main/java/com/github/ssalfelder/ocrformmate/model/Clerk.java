package com.github.ssalfelder.ocrformmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Mitarbeiter")
public class Clerk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Vorname", nullable = false)
    private String firstname;

    @Column(name = "Nachname", nullable = false)
    private String lastname;

    @Column(name = "Postleitzahl", nullable = false)
    private String postalCode;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Passwort", nullable = false)
    private String password;

    @Column(name = "Erstellt", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "Aktualisiert")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    private String secret;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
