package com.example.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController - 회원가입·로그인·내 정보 API (회원 서비스, 포트 8081)
 *
 *   주소                 방식   로그인   하는 일                 성공
 *   /api/auth/signup     POST   -        회원가입                 201
 *   /api/auth/login      POST   -        로그인 → 토큰 발급        200
 *   /api/auth/me         GET    필요     토큰 주인의 정보          200
 *
 * [MSA 개념] "로그인 필요" 검사는 이 서비스가 하지 않는다
 *   React 는 항상 게이트웨이(8080)로 요청한다. 게이트웨이가 토큰을 검사하고,
 *   통과하면 요청 헤더에 "X-User-Id: 3" 을 붙여서 이 서비스로 넘겨 준다.
 *   그래서 이 서비스는 토큰을 볼 필요 없이 @RequestHeader("X-User-Id") 로 번호만 꺼내 쓰면 된다.
 *   (X-User-Id 헤더 없이 8081 로 직접 부르면 GlobalExceptionHandler 가 401 로 응답한다)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.signup(request);
	}

	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public UserResponse me(@RequestHeader("X-User-Id") Long userId) { // 게이트웨이가 붙여 준 사용자 번호
		return authService.me(userId);
	}
}
