package com.coderrr1ck.backend.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmailAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrue(String email);
}
