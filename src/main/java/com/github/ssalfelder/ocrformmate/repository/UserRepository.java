package com.github.ssalfelder.ocrformmate.repository;

import com.github.ssalfelder.ocrformmate.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findBySecret(String secret);
}
