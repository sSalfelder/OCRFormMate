package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.Admin;
import com.github.ssalfelder.ocrformmate.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isSecretValid(Integer adminId, String inputSecret) {
        // Sucht den Admin mit der übergebenen ID
        Admin admin = adminRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));
        // Vergleicht den in der Datenbank gespeicherten Wert mit der Benutzereingabe
        return admin.getSecret().equals(inputSecret);
    }
}
