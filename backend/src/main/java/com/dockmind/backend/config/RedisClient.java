package com.dockmind.backend.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisClient {

	private final StringRedisTemplate redisTemplate;
	
	public void set(String key, String value, long ttlSeconds) {
		redisTemplate.opsForValue().set(key,value,ttlSeconds);
	}
	
	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	public void delete(String key) {
		redisTemplate.delete(key);
	}
	
	public boolean exists(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}
}
