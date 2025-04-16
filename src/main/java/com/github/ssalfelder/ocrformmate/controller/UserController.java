package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.dto.UserLoginDTO;
import com.github.ssalfelder.ocrformmate.dto.UserRegistrationDTO;
import com.github.ssalfelder.ocrformmate.model.Address;
import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.repository.AddressRepository;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    /**
     * Registrierung eines neuen Users über DTO
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO dto) {
        try {
            System.out.println("Registrierung gestartet");
            System.out.println("DTO erhalten: ");

            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Benutzer mit dieser E-Mail existiert bereits.");
            }

            String fullStreet = dto.getStreet() + " " + dto.getHouseNumber();

            // Adresse finden oder neu anlegen
            Optional<Address> existing = addressRepository.findByStreetAndPostalCodeAndCity(
                    fullStreet, dto.getPostalCode(), dto.getCity()
            );

            Address address = existing.orElseGet(() -> {
                Address newAddress = new Address();
                newAddress.setStreet(fullStreet);
                newAddress.setPostalCode(dto.getPostalCode());
                newAddress.setCity(dto.getCity());
                return addressRepository.save(newAddress);
            });

            User user = new User();
            user.setFirstname(dto.getFirstname());
            user.setLastname(dto.getLastname());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setAddress(address);
            user.setSecret(UUID.randomUUID().toString());


            User savedUser = userRepository.save(user);

            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();

            String errorClass = ex.getClass().getSimpleName();
            String errorMessage = ex.getMessage() != null ? ex.getMessage() : "<keine Nachricht>";
            String cause = (ex.getCause() != null) ? ex.getCause().toString() : "<keine Ursache>";

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler: " + errorClass + " – " + errorMessage + " – caused by: " + cause);
        }
    }

    /**
     * Benutzer anhand der ID abrufen
     */
    @GetMapping
    public ResponseEntity<?> get(@RequestParam(value = "id") int id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No user found with id " + id, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Benutzer anhand von E-Mail + Passwort validieren
     */
    @PostMapping("/validate")
    public ResponseEntity<User> validate(@RequestBody UserLoginDTO login) {
        return userRepository.findByEmail(login.getEmail())
                .filter(user -> passwordEncoder.matches(login.getPassword(), user.getPassword()))
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("UserController aktiv");
    }

}
