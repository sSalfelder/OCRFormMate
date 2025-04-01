package com.github.ssalfelder.ocrformmate.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocr_result")
public class OcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recognized_text", length = 5000)
    private String recognizedText;

    private LocalDateTime createdAt;

    @Column(name = "Behörde")
    private String authority;

    // für Bürger
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // für Sachbearbeiter
    @ManyToOne
    @JoinColumn(name = "clerk_id", referencedColumnName = "id")
    private Clerk clerk;

    // ggf. weitere Felder: Dateiname, Dokumenttyp etc.

    public OcrResult() {
        this.createdAt = LocalDateTime.now();
    }

    public OcrResult(String recognizedText) {
        this.recognizedText = recognizedText;
        this.createdAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Clerk getClerk() {
        return clerk;
    }

    public void setClerk(Clerk clerk) {
        this.clerk = clerk;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
