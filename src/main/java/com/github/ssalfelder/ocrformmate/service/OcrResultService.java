package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OcrResultService {

    @Autowired
    private OcrResultRepository ocrResultRepository;

    public OcrResult saveOcrResult(String recognizedText) {
        OcrResult ocrResult = new OcrResult(recognizedText);
        return ocrResultRepository.save(ocrResult);
    }
}
