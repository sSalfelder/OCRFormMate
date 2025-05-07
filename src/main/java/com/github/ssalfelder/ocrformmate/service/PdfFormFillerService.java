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

    private static final Map<String, String> FIELD_NAME_MAP = Map.ofEntries(
            Map.entry("familienname", "txtfPersonNachname"),
            Map.entry("vorname", "txtfPersonVorname"),
            Map.entry("geburtsdatum", "datePersonGebDatum"),
            Map.entry("geburtsname", "txtfPersonGebName"),
            Map.entry("geburtsort", "txtfPersonGebOrt"),
            Map.entry("geburtsland", "txtfPersonGebLand"),
            Map.entry("staatsangehoerigkeit", "txtfPersonStaatsangehoerigkeit"),
            Map.entry("telefonnummer", "txtfPersonTel"),
            Map.entry("wohnort", "txtfPersonOrt"),
            Map.entry("postleitzahl", "txtfPersonPostfach"),
            Map.entry("rentenversicherungsnummer", "txtfPersonSVRNr")
    );




    public void fillForm(File templatePdf, File outPdf, Map<String, String> ocrValues) throws IOException {
        try (PDDocument doc = PDDocument.load(templatePdf)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            if (form == null) throw new IllegalStateException("Kein AcroForm im PDF!");

            form.setNeedAppearances(true);

            System.out.println("→ Alle Formularfelder im Template:");
            for (PDField field : form.getFieldTree()) {
                System.out.println("   • " + field.getFullyQualifiedName());
            }

            for (var entry : ocrValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String pdfFieldName = FIELD_NAME_MAP.get(key);

                if (pdfFieldName != null) {
                    PDField field = form.getField(pdfFieldName);
                    if (field != null) {
                        try {
                            field.setValue(value);
                            field.setReadOnly(false); // optional
                            System.out.println("Eingetragen: " + pdfFieldName + " = " + value);
                        } catch (Exception e) {
                            System.err.println("Fehler beim Setzen von " + pdfFieldName + ": " + e.getMessage());
                        }
                    } else {
                        System.err.println("Feld nicht gefunden: " + pdfFieldName);
                    }
                }
            }


            form.refreshAppearances();

            File parent = outPdf.getParentFile();
            if (!parent.exists()) parent.mkdirs();

            doc.save(outPdf);
            System.out.println("PDF gespeichert unter: " + outPdf.getAbsolutePath());
        }
    }
}

