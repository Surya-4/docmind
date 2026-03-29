package com.dockmind.backend.auth;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.expiration}")
	private long expiration;
	
	private Key getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	public String generateToken(String email) {
		return Jwts.builder().
				setSubject(email).
				setExpiration(new Date(System.currentTimeMillis()+expiration)).
				setIssuedAt(new Date()).
				signWith(getKey(),SignatureAlgorithm.HS256).
				compact();
	}
	
	public String getEmail(String token) {
		return Jwts.parserBuilder().
				setSigningKey(getKey()).
				build().
				parseClaimsJws(token).
				getBody().
				getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().
			setSigningKey(getKey()).
			build().
			parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
}
