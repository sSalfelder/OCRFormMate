package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.dto.ClerkLoginDTO;
import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/clerk")
public class ClerkController {

    @Autowired
    private ClerkRepository clerkRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Clerk> register(@RequestBody Clerk newClerk) {

        String hashedPassword = passwordEncoder.encode(newClerk.getPassword());
        newClerk.setPassword(hashedPassword);

        // Generate API secret
        newClerk.setSecret(UUID.randomUUID().toString());

        Clerk savedClerk = clerkRepository.save(newClerk);
        savedClerk.setPassword(null);

        return new ResponseEntity<>(savedClerk, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam("id") int id) {
        Optional<Clerk> clerk = clerkRepository.findById(id);
        if (clerk.isPresent()) {
            Clerk result = clerk.get();
            result.setPassword(null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>("No user found with id " + id, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ClerkLoginDTO login) {
        Optional<Clerk> optionalClerk = clerkRepository.findByEmail(login.getEmail());

        if (optionalClerk.isPresent()) {
            Clerk clerk = optionalClerk.get();

            if (passwordEncoder.matches(login.getPassword(), clerk.getPassword())) {
                return ResponseEntity.ok().body(clerk.getSecret());
            }
        }

        return new ResponseEntity<>("Wrong credentials / not found.", HttpStatus.UNAUTHORIZED);
    }
}
