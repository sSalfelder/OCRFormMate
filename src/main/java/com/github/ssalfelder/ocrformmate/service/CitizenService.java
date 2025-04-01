package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenService {

    private final OcrResultRepository ocrResultRepository;
    private final UserRepository userRepository;

    public CitizenService(OcrResultRepository ocrResultRepository, UserRepository userRepository) {
        this.ocrResultRepository = ocrResultRepository;
        this.userRepository = userRepository;
    }

}
