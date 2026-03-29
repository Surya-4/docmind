package com.dockmind.backend.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dockmind.backend.entity.User;

public interface UserRepository extends JpaRepository<User,UUID>{
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
