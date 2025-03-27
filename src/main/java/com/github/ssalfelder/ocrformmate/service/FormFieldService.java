package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class FormFieldService {

    // Simulierter statischer Template-Mapping (könnte auch aus JSON geladen werden)
    private static final Map<String, List<Rect>> FORM_TEMPLATES = Map.of(
            "BUERGERGELD", List.of(
                    new Rect(212, 1113, 711, 127), // Familienname
                    new Rect(922, 1123, 852, 91),  // Nachname
                    new Rect(213, 1252, 943, 124),  // Geburtsname
                    new Rect(1156, 1256, 616, 120), // Geburtsdatum
                    new Rect(212, 1405, 708, 110), // Geburtsort
                    new Rect(922, 1404, 849, 114),  // Geburtsland
                    new Rect(211, 1540, 474, 108),  // Geschlecht
                    new Rect(686, 1540, 1086, 108), // Staatsangehörigkeit
                    new Rect(216, 1767, 1556, 109),  // Einreisedatum
                    new Rect(213, 1918, 701, 115),  // Rentenversicherungsnummer
                    new Rect(217, 2059, 1555, 131), // Straße, Hausnummer
                    new Rect(212, 2205, 1560, 99), // ggf. wohnhaft bei
                    new Rect(216, 2340, 469, 114), // Postleitzahl
                    new Rect(687, 2350, 1087, 111), // Wohnort
                    new Rect(212, 2542, 708, 123), // Telefonnummer
                    new Rect(924, 2552, 851, 118), // E-Mail-Adresse
                    new Rect(1134, 2679, 633, 123), // Antragstellung: späterer ZP
                    new Rect(641, 2808, 1133, 98), // Antragstellung: Folgemonat
                    new Rect(785, 3105, 986, 93), // Familienstand: getrennt lebend
                    new Rect(566, 3189, 1204, 89), // Familienstand: geschieden seit
                    new Rect(940, 3265, 832, 119) // Familienstand: aufgehobene LP
            ),
            "ANMELDUNG", List.of(
                    new Rect(120, 300, 180, 50), // Straße
                    new Rect(320, 300, 180, 50)  // Ort
            )
    );

    public Mat overlayTemplateFields(Mat alignedDoc, String formType) {
        List<Rect> fields = FORM_TEMPLATES.getOrDefault(formType.toUpperCase(), List.of());

        for (Rect rect : fields) {
            rectangle(alignedDoc, rect, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
        }

        System.out.println("🧩 Felder aus Template [" + formType + "] gezeichnet: " + fields.size());

        imwrite("form_preview.png", alignedDoc);
        return alignedDoc;
    }
}

