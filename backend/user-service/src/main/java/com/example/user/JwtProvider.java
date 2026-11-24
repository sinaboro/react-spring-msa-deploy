package com.example.user;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtProvider - JWT(로그인 토큰) "만들기" 전용
 *
 * [MSA 개념] 토큰은 회원 서비스가 만들고, 검사는 게이트웨이가 한다
 *   - 회원 서비스 : 로그인에 성공하면 비밀 열쇠로 서명한 토큰을 발급한다 (이 파일)
 *   - 게이트웨이  : 요청마다 같은 비밀 열쇠로 토큰이 진짜인지 확인한다 (api-gateway 의 JwtChecker)
 *   두 서비스가 같은 열쇠(jwt.secret)를 나눠 가지므로, 게이트웨이는 회원 서비스나 userdb 에
 *   물어보지 않고도 "누가 보낸 요청인지" 알 수 있다.
 *
 * 토큰 안에 넣는 것 (토큰 내용은 누구나 읽을 수 있으므로 비밀번호는 절대 넣지 않는다)
 *   sub      : 사용자 번호 (예: "3")
 *   nickname : 닉네임 (예: "민수") ← 모놀리식 판에는 없던 것.
 *              게시글 서비스가 글쓴이 이름을 알아야 하는데 userdb 를 볼 수 없으니 토큰에 실어 보낸다.
 *   iat, exp : 발급 시각, 만료 시각
 */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final Duration expiration;

	public JwtProvider(@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-minutes}") long expirationMinutes) {
		// HS256 은 32바이트(256비트) 이상의 열쇠가 필요하다. 짧으면 서버가 켜질 때 오류가 난다
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiration = Duration.ofMinutes(expirationMinutes);
	}

	/** 회원 정보를 담은 토큰을 만든다 */
	public String createToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(user.getId())) //        sub: 토큰 주인 번호 (문자열로 넣는 것이 규칙)
				.claim("nickname", user.getNickname()) //        nickname: 글쓴이 이름으로 쓰인다
				.issuedAt(Date.from(now)) //                     iat: 발급 시각
				.expiration(Date.from(now.plus(expiration))) //  exp: 만료 시각
				.signWith(key) //                                서명 (열쇠 길이를 보고 HS256 을 고른다)
				.compact(); //                                   "헤더.내용.서명" 글자로
	}
}
