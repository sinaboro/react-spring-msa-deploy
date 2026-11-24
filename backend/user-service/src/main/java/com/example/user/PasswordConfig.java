package com.example.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordConfig - 비밀번호 해시 도구(BCrypt)를 Bean 으로 등록
 *
 *   encode("password123")        → "$2a$10$Q5..." (매번 다른 값. 소금(salt)을 섞기 때문)
 *   matches("password123", 해시)  → true / false
 *
 * 모놀리식 판에서는 SecurityConfig 안에 있었다. 이 서비스는 Spring Security 전체를 쓰지 않고
 * 해시 도구(spring-security-crypto)만 쓰므로, 이 작은 설정 파일 하나면 된다.
 */
@Configuration
public class PasswordConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
