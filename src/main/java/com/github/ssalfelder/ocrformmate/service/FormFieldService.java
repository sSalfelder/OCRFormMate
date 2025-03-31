package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class FormFieldService {

    private static final Map<String, Rect> BUERGERGELDFIELDS = Map.ofEntries(
            Map.entry("familienname", new Rect(212, 1113, 711, 127)),
            Map.entry("vorname", new Rect(922, 1123, 852, 91)),
            Map.entry("geburtsname", new Rect(213, 1252, 943, 124)),
            Map.entry("geburtsdatum", new Rect(1156, 1256, 616, 120)),
            Map.entry("geburtsort", new Rect(212, 1405, 708, 110)),
            Map.entry("geburtsland", new Rect(922, 1404, 849, 114)),
            Map.entry("geschlecht", new Rect(211, 1540, 474, 108)),
            Map.entry("staatsangehoerigkeit", new Rect(686, 1540, 1086, 108)),
            Map.entry("einreisedatum", new Rect(216, 1767, 1556, 109)),
            Map.entry("rentenversicherungsnummer", new Rect(213, 1918, 701, 115)),
            Map.entry("strasse_hausnummer", new Rect(217, 2059, 1555, 131)),
            Map.entry("wohnhaft_bei", new Rect(212, 2205, 1560, 99)),
            Map.entry("postleitzahl", new Rect(216, 2340, 469, 114)),
            Map.entry("wohnort", new Rect(687, 2350, 1087, 111)),
            Map.entry("telefonnummer", new Rect(212, 2542, 708, 123)),
            Map.entry("email", new Rect(924, 2552, 851, 118)),
            Map.entry("antrag_spaeter", new Rect(1134, 2679, 633, 123)),
            Map.entry("antrag_folgemonat", new Rect(641, 2808, 1133, 98)),
            Map.entry("familienstand_getrennt", new Rect(785, 3105, 986, 93)),
            Map.entry("familienstand_geschieden", new Rect(566, 3189, 1204, 89)),
            Map.entry("familienstand_lp", new Rect(940, 3265, 832, 119))
    );

    private static final Map<String, Map<String, Rect>> FORM_TEMPLATES = Map.of(
            "BUERGERGELD", BUERGERGELDFIELDS,
            "ANMELDUNG", Map.of(
                    "strasse", new Rect(120, 300, 180, 50),
                    "ort", new Rect(320, 300, 180, 50)
            )
    );


    /**
     * Zeichnet die Felder eines Templates als grüne Rechtecke ins Dokument.
     */
    public Mat overlayTemplateFields(Mat alignedDoc, String formType) {
        Map<String, Rect> fields = getTemplateFields(formType);
        for (Rect rect : fields.values()) {
            rectangle(alignedDoc, rect, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
        }

        System.out.println("🧩 Felder aus Template [" + formType + "] gezeichnet: " + fields.size());
        imwrite("form_preview.png", alignedDoc);
        return alignedDoc;
    }

    /**
     * Gibt die benannten Felder eines Templates zurück.
     */
    public Map<String, Rect> getTemplateFields(String formType) {
        return FORM_TEMPLATES.getOrDefault(formType.toUpperCase(), Map.of());
    }
}

