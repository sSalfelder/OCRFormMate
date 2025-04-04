package com.github.ssalfelder.ocrformmate.service;

import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class OcrService {
    private HandwritingClient handwritingClient;

    public OcrService() {
        this.handwritingClient = new HandwritingClient();
    }

    public Map<String, String> recognizeFields(File imageFile, Map<String, Rect> fields, String formType) throws Exception {
        return handwritingClient.recognizeWithFields(imageFile, fields, formType);
    }
}