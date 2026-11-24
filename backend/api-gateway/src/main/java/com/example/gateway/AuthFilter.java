package com.example.gateway;

import io.jsonwebtoken.Claims;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * AuthFilter - 게이트웨이를 지나가는 "모든" 요청의 로그인 토큰을 검사하는 필터
 *
 * [MSA 개념] 인증은 정문(게이트웨이)에서 한 번만
 *   서비스가 10개라도 토큰 검사 코드는 여기 한 곳에만 있다.
 *   검사를 통과한 요청에는 "누가 보냈는지" 를 헤더로 적어서 서비스에 넘긴다.
 *     X-User-Id       : 3
 *     X-User-Nickname : %EB%AF%BC%EC%88%98   ("민수". 헤더에는 한글을 바로 못 실어서 URL 인코딩)
 *   서비스는 이 헤더만 읽으면 된다 (토큰·Spring Security 를 몰라도 된다).
 *
 * 하는 일 (순서대로)
 *   1. 손님(브라우저)이 보낸 X-User-Id, X-User-Nickname 헤더는 지운다.
 *      → 누군가 "X-User-Id: 1" 을 직접 적어 보내 남인 척하는 것을 막는다. (헤더는 게이트웨이만 붙일 수 있다)
 *   2. "Authorization: Bearer <토큰>" 에서 토큰을 꺼내 검사한다.
 *   3. 로그인이 필요한 주소인데 올바른 토큰이 없으면 → 401 { "detail": "..." } 로 바로 돌려보낸다.
 *   4. 토큰이 올바르면 X-User-Id, X-User-Nickname 헤더를 붙여 서비스로 넘긴다.
 *
 * 로그인 없이 쓸 수 있는 주소
 *   GET  /api/posts, /api/posts/{id}           (글 읽기)
 *   POST /api/auth/signup, /api/auth/login      (가입, 로그인)
 *   그 밖 (글쓰기·수정·삭제, /api/auth/me) 은 로그인 필요
 *
 * 처음 보는 이름 정리 (게이트웨이는 WebFlux 라는 방식으로 동작한다)
 *   GlobalFilter         : 모든 경로(route)에 적용되는 게이트웨이 필터
 *   ServerWebExchange    : 요청(request) + 응답(response) 묶음
 *   Mono<Void>           : "나중에 끝나는 작업" 이라는 뜻. 지금은 "return chain.filter(...)" 또는
 *                          "return 응답쓰기" 둘 중 하나를 돌려준다고만 알면 충분하다.
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

	private static final String USER_ID = "X-User-Id";
	private static final String USER_NICKNAME = "X-User-Nickname";

	private final JwtChecker jwtChecker;

	public AuthFilter(JwtChecker jwtChecker) {
		this.jwtChecker = jwtChecker;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		// 1. 손님이 보낸 X-User-* 헤더 지우기 (위조 방지)
		ServerHttpRequest.Builder newRequest = request.mutate()
				.headers(headers -> {
					headers.remove(USER_ID);
					headers.remove(USER_NICKNAME);
				});

		// 2. 토큰 꺼내서 검사
		String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		boolean hasToken = authorization != null && authorization.startsWith("Bearer ");
		Claims claims = hasToken ? jwtChecker.check(authorization.substring(7)) : null; // "Bearer " 뒤의 글자

		// 3. 로그인이 필요한데 올바른 토큰이 없다 → 401
		if (claims == null && needsLogin(request)) {
			String message = hasToken ? "로그인이 만료되었습니다. 다시 로그인하세요" : "로그인이 필요합니다";
			return unauthorized(exchange, message);
		}

		// 4. 토큰이 올바르면 "누가 보냈는지" 헤더로 붙이기
		if (claims != null) {
			String nickname = claims.get("nickname", String.class);
			newRequest.header(USER_ID, claims.getSubject());
			newRequest.header(USER_NICKNAME, URLEncoder.encode(nickname, StandardCharsets.UTF_8)); // 민수 → %EB%AF%BC...
		}

		// 바뀐 요청으로 다음 단계(→ 서비스로 전달)를 진행한다
		return chain.filter(exchange.mutate().request(newRequest.build()).build());
	}

	/** 이 요청은 로그인이 필요한가? */
	private boolean needsLogin(ServerHttpRequest request) {
		HttpMethod method = request.getMethod();
		String path = request.getPath().value(); // 예: /api/posts/5

		if (method == HttpMethod.GET && path.startsWith("/api/posts")) {
			return false; // 글 읽기는 누구나
		}
		if (method == HttpMethod.POST && (path.equals("/api/auth/signup") || path.equals("/api/auth/login"))) {
			return false; // 가입·로그인은 누구나
		}
		return true; // 나머지는 로그인 필요
	}

	/** 401 응답을 { "detail": "..." } JSON 으로 바로 돌려준다 (서비스까지 가지 않는다) */
	private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		byte[] body = ("{\"detail\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
		return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
	}

	/** 필터 순서. 숫자가 작을수록 먼저 실행된다 → 서비스로 전달하기 전에 반드시 먼저 검사 */
	@Override
	public int getOrder() {
		return -1;
	}
}
