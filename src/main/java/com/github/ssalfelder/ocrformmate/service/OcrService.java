package com.github.ssalfelder.ocrformmate.service;

import java.io.File;

public class OcrService {
    private HandwritingClient handwritingClient;

    public OcrService() {
        this.handwritingClient = new HandwritingClient();
    }

    public String handleHandwritingOCR(File inputFile) throws Exception {
        return handwritingClient.recognize(inputFile);
    }

}



