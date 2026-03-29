package com.dockmind.backend.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dockmind.backend.dto.AuthRequest;
import com.dockmind.backend.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	
	public String register(AuthRequest request) {
		if(userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email exists already");
		}
		User user=new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		userRepository.save(user);
		return jwtUtil.generateToken(request.getEmail());
	}
	
	public String login(AuthRequest request) {
		User user=userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new RuntimeException("User does not exist"));
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new RuntimeException("Email or Password is incorrect");
		}
		return jwtUtil.generateToken(user.getEmail());
	}
}
