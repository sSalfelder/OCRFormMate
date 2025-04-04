package com.github.ssalfelder.ocrformmate.session;

public class OcrSessionHolder {
    private static String recognizedText;
    private static String formType;

    public static void set(String text) {
        recognizedText = text;
    }

    public static String get() {
        return recognizedText;
    }

    public static String getFormType() {
        return formType;
    }

    public static void setFormType(String formType) {
        OcrSessionHolder.formType = formType;
    }

    public static boolean isAvailable() {
        return recognizedText != null && !recognizedText.isBlank();
    }

    public static void clear() {
        recognizedText = null;
        formType = null;
    }

}
