package com.example.user;

import com.example.user.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - 회원가입·로그인·내 정보 규칙
 *
 *   가입   : 아이디 중복이면 409 → 비밀번호를 BCrypt 해시로 바꿔 저장
 *   로그인 : 아이디로 회원 찾기 → 비밀번호와 해시 비교 → 맞으면 JWT 발급
 *   내 정보 : 게이트웨이가 알려 준 사용자 번호로 회원을 찾아 돌려준다
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
	}

	@Transactional
	public UserResponse signup(SignupRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다"); // 409
		}
		String hash = passwordEncoder.encode(request.password()); // 비밀번호 → 해시
		User user = userRepository.save(new User(request.username(), hash, request.nickname().strip()));
		return UserResponse.from(user);
	}

	public TokenResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.username())
				// 아이디가 없을 때와 비밀번호가 틀릴 때 같은 메시지를 준다
				// → "이 아이디는 가입되어 있다" 는 정보를 공격자에게 주지 않기 위해
				.filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다"));
		String token = jwtProvider.createToken(user); // 번호 + 닉네임을 담은 토큰
		return TokenResponse.bearer(token, UserResponse.from(user));
	}

	public UserResponse me(Long userId) {
		return userRepository.findById(userId)
				.map(UserResponse::from)
				// 토큰은 진짜인데 회원이 없다 = 토큰 발급 뒤에 DB 를 지웠다 → 다시 로그인하게 한다
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인하세요"));
	}
}
