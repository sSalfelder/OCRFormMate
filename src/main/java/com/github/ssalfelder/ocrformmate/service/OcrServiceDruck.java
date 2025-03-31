package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Rect;

import java.io.File;
import java.util.Map;

public class OcrServiceDruck {
    private HandwritingClient handwritingClient;

    public OcrServiceDruck() {
        this.handwritingClient = new HandwritingClient();
    }

    public String handleHandwritingOCR(File inputFile) throws Exception {
        return handwritingClient.recognize(inputFile);
    }

    public Map<String, String> recognizeFields(File imageFile, Map<String, Rect> fields, String formType) throws Exception {
        return handwritingClient.recognizeWithFields(imageFile, fields, formType);
    }
}