package com.github.ssalfelder.ocrformmate.controller;

import com.github.ssalfelder.ocrformmate.model.Address;
import com.github.ssalfelder.ocrformmate.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping
    public Iterable<Address> getAll() {
        return addressRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Address> create(@RequestBody Address address) {
        Address saved = addressRepository.save(address);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
