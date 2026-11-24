package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * UserServiceApplication - 회원 서비스의 시작점 (포트 8081)
 *
 * IntelliJ 에서 이 파일을 열고 class 왼쪽의 초록색 ▶ 버튼을 누르면 회원 서비스가 켜진다.
 * (터미널에서는 backend 폴더에서 .\gradlew.bat :user-service:bootRun)
 *
 * [MSA 개념] 이 서비스가 맡은 일 - "회원" 한 가지
 *   - 회원가입 (비밀번호는 BCrypt 해시로 저장)
 *   - 로그인 → JWT 토큰 발급
 *   - 내 정보 보기
 *   게시글은 전혀 모른다. 게시글 일은 post-service 가 한다.
 *   이렇게 "한 가지 업무" 단위로 나눈 작은 서버를 마이크로서비스라고 부른다.
 */
@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
