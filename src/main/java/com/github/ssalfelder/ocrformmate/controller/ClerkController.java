package com.github.ssalfelder.ocrformmate.controller;


import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ClerkController {

    @Autowired
    private ClerkRepository clerkRepository;

    @PostMapping("/clerk/register")
    private ResponseEntity<Clerk> register(@RequestBody Clerk newClerk) {
        //generate secret
        newClerk.setSecret(UUID.randomUUID().toString());

        var savedClerk = clerkRepository.save(newClerk);
        return new ResponseEntity<Clerk>(savedClerk, HttpStatus.CREATED);
    }

    @GetMapping("/clerk")
    private ResponseEntity<Clerk> get(@RequestParam(value = "id") int id) {
        var clerk = clerkRepository.findById(id);
        if (clerk.isPresent()) {
            return new ResponseEntity<Clerk>(clerk.get(), HttpStatus.OK);
        }
        return new ResponseEntity("No user found with id " + id, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/clerk/validate")
    private ResponseEntity<String> validate(@RequestParam(value = "email") String email,
                                            @RequestParam(value = "password") String password) {
        var validClerk = clerkRepository.findByEmailAndPassword(email, password);
        if (validClerk.isPresent()) {
            return new ResponseEntity<String>("API Secret: " + validClerk.get().getSecret(), HttpStatus.OK);
        }

        return new ResponseEntity("Wrong credentials / not found.", HttpStatus.NOT_FOUND);
    }
}