package com.dockmind.backend.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
		ResponseCookie cookie=ResponseCookie.from("jwt", token).
		secure(true).
		sameSite("None").
		httpOnly(true).
		path("/").
		maxAge(86400)
		.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return ResponseEntity.status(HttpStatus.CREATED).body("Registration Successfull");
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody AuthRequest request, HttpServletResponse response){
		String token=authService.login(request);
		ResponseCookie cookie=ResponseCookie.from("jwt", token).
		httpOnly(true).
		secure(true).
		sameSite("None").
		path("/").
		maxAge(86400).build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return ResponseEntity.status(HttpStatus.OK).body("Login Successfull");
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletResponse response){
		ResponseCookie cookie=ResponseCookie.from("jwt", "").
				httpOnly(true).
				secure(true).
				sameSite("None").
				path("/").
				maxAge(0).build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return ResponseEntity.ok("Logout Successfull");
	}
}
