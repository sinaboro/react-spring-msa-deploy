package com.example.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PostServiceApplication - 게시글 서비스의 시작점 (포트 8082)
 *
 * IntelliJ 에서 이 파일을 열고 class 왼쪽의 초록색 ▶ 버튼을 누르면 게시글 서비스가 켜진다.
 * (터미널에서는 backend 폴더에서 .\gradlew.bat :post-service:bootRun)
 *
 * [MSA 개념] 이 서비스가 맡은 일 - "게시글" 한 가지
 *   - 글 목록 · 글 하나 · 쓰기 · 수정 · 삭제
 *   - 본인 글인지 검사 (남의 글은 403)
 *   회원가입·로그인·토큰은 전혀 모른다. 그 일은 user-service 와 api-gateway 가 한다.
 *   회원 서비스가 꺼져 있어도, 이미 로그인한 사람은 글을 읽고 쓸 수 있다 (장애가 번지지 않는다).
 */
@SpringBootApplication
public class PostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostServiceApplication.class, args);
	}
}
