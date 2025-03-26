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

    // ggf. weitere Felder: Dateiname, Dokumenttyp etc.

    public OcrResult() {
        this.createdAt = LocalDateTime.now();
    }

    public OcrResult(String recognizedText) {
        this.recognizedText = recognizedText;
        this.createdAt = LocalDateTime.now();
    }

}
