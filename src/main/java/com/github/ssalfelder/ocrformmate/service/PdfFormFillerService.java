package com.github.ssalfelder.ocrformmate.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfFormFillerService {
    private static final Map<String,String> FIELD_NAME_MAP = Map.of(
            "familienname",     "PersFam",
            "vorname",          "PersVorn",
            "geburtsdatum",     "PersGebDat"
    );

    public void fillForm(File templatePdf, File outPdf, Map<String,String> ocrValues) throws IOException {
        try (PDDocument doc = PDDocument.load(templatePdf)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form == null) throw new IllegalStateException("Kein AcroForm im PDF!");

            System.out.println("→ Alle Formularfelder im Template:");
            form.getFields().forEach(f -> System.out.println("   • " + f.getFullyQualifiedName()));

            for (var entry : ocrValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String pdfFieldName = FIELD_NAME_MAP.get(key);
                if (pdfFieldName != null) {
                    PDField field = form.getField(pdfFieldName);
                    if (field != null) {
                        field.setValue(value);
                    } else {
                        System.err.println("Feld nicht gefunden: " + pdfFieldName);
                    }
                }
            }
            doc.save(outPdf);
        }
    }
}
