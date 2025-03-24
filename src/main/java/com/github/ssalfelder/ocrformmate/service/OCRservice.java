package com.github.ssalfelder.ocrformmate.service;

import java.io.File;

public class OCRservice {
    private HandwritingClient handwritingClient;

        protected String handleHandwritingOCR(File inputFile) throws Exception {
            return handwritingClient.recognize(inputFile);
        }

}


