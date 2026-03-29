package com.dockmind.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.dockmind.backend.dto.AuthRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody AuthRequest request, HttpServletResponse response){
		String token=authService.register(request);
		Cookie cookie=new Cookie("jwt", token);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(86400);
		response.addCookie(cookie);
		return ResponseEntity.status(HttpStatus.CREATED).body("Registration Successfull");
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest request, HttpServletResponse response){
		String token=authService.login(request);
		Cookie cookie=new Cookie("jwt", token);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(86400);
		response.addCookie(cookie);
		return ResponseEntity.status(HttpStatus.OK).body("Login Successfull");
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response){
		Cookie cookie=new Cookie("jwt", "");
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		response.addCookie(cookie);
		return ResponseEntity.ok("Logout Successfull");
	}
}
