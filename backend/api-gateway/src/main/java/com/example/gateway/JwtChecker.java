package com.example.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtChecker - JWT(로그인 토큰) "검사" 전용
 *
 * user-service 의 JwtProvider 가 만든 토큰을, 같은 비밀 열쇠(jwt.secret)로 검사한다.
 *   - 서명이 맞는가?  (누군가 토큰 내용을 몰래 고쳤다면 서명이 맞지 않는다)
 *   - 만료되지 않았는가?
 * 둘 다 통과하면 토큰 내용(Claims)을 돌려준다.
 *   claims.getSubject()              → "3"     (사용자 번호)
 *   claims.get("nickname", String.class) → "민수"  (닉네임)
 *
 * [MSA 개념] 게이트웨이는 userdb 를 보지 않는다. 서명만 확인하면 토큰을 믿을 수 있기 때문이다.
 */
@Component
public class JwtChecker {

	private final SecretKey key;

	public JwtChecker(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/** 토큰이 올바르면 내용(Claims)을, 위조됐거나 만료됐으면 null 을 돌려준다 */
	public Claims check(String token) {
		try {
			return Jwts.parser().verifyWith(key).build()
					.parseSignedClaims(token) // 서명·만료를 검사한다. 문제가 있으면 JwtException
					.getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
