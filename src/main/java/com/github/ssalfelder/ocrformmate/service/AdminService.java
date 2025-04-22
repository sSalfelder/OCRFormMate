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
        Admin admin = adminRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin nicht gefunden"));
        return admin.getSecret().equals(inputSecret);
    }
}
