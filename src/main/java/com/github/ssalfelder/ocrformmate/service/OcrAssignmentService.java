package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.auth.ClerkSessionHolder;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.model.OcrResult;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import com.github.ssalfelder.ocrformmate.repository.OcrResultRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OcrAssignmentService {

    private final OcrResultRepository ocrResultRepository;
    private final UserRepository userRepository;
    private final ClerkRepository clerkRepository;

    public OcrAssignmentService(OcrResultRepository ocrResultRepository,
                                UserRepository userRepository,
                                ClerkRepository clerkRepository) {
        this.ocrResultRepository = ocrResultRepository;
        this.userRepository = userRepository;
        this.clerkRepository = clerkRepository;
    }

    public OcrResult saveForUser(String text, int userId, String authority) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        OcrResult result = new OcrResult();
        result.setRecognizedText(text);
        result.setUser(user);
        result.setAuthority(authority);
        return ocrResultRepository.save(result);
    }

    public OcrResult saveForLoggedInClerk(String text) {
        Clerk clerk = ClerkSessionHolder.getLoggedInClerk();

        if (clerk == null) throw new IllegalStateException("Kein Clerk eingeloggt");

        OcrResult result = new OcrResult();
        result.setRecognizedText(text);
        result.setClerk(clerk);
        result.setAuthority(clerk.getAuthority());
        return ocrResultRepository.save(result);
    }

}
