package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.Admin;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminRepository extends CrudRepository<Admin, Integer> {
    Optional<Admin> findByEmailAndPassword(String email, String password);
    Optional<Admin> findBySecret(String secret);
    Optional<Admin> findById(int id);
}
