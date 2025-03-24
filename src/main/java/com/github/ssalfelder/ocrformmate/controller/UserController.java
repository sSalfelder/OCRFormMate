package com.github.ssalfelder.ocrformmate.controller;


import com.github.ssalfelder.ocrformmate.model.User;
import com.github.ssalfelder.ocrformmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user/register")
    private ResponseEntity<User> register(@RequestBody User newUser) {
        //generate secret
        newUser.setSecret(UUID.randomUUID().toString());

        var saveduser = userRepository.save(newUser);
        return new ResponseEntity<User>(saveduser, HttpStatus.CREATED);
    }

    @GetMapping("/user")
    private ResponseEntity<User> get(@RequestParam(value = "id") int id) {
        var user = userRepository.findById(id);
        if (user.isPresent()) {
            return new ResponseEntity<User>(user.get(), HttpStatus.OK);
        }
        return new ResponseEntity("No user found with id " + id, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/user/validate")
    private ResponseEntity<String> validate(@RequestParam(value = "email") String email,
                                            @RequestParam(value = "password") String password) {
        var validUser = userRepository.findByEmailAndPassword(email, password);
        if (validUser.isPresent()) {
            return new ResponseEntity<String>("API Secret: " + validUser.get().getSecret(), HttpStatus.OK);
        }

        return new ResponseEntity("Wrong credentials / not found.", HttpStatus.NOT_FOUND);
    }
}