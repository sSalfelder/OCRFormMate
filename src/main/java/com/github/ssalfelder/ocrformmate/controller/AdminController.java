package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.model.Admin;
import com.github.ssalfelder.ocrformmate.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/register")
    private ResponseEntity<Admin> register (@RequestBody Admin newAdmin) {
        //generate secret
        newAdmin.setSecret(UUID.randomUUID().toString());

        var savedAdmin = adminRepository.save(newAdmin);
        return new ResponseEntity<Admin>(savedAdmin, HttpStatus.CREATED);
    }

}