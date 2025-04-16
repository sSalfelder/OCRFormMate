package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.Clerk;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClerkRepository extends CrudRepository<Clerk, Integer> {

    Optional<Clerk> findByEmail(String email);
    Optional<Clerk> findBySecret(String secret);
}
