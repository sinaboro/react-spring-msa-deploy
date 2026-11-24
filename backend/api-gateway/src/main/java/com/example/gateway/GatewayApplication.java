package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GatewayApplication - API 게이트웨이의 시작점 (포트 8080)
 *
 * IntelliJ 에서 이 파일을 열고 class 왼쪽의 초록색 ▶ 버튼을 누르면 게이트웨이가 켜진다.
 * (터미널에서는 backend 폴더에서 .\gradlew.bat :api-gateway:bootRun)
 *
 * [MSA 개념] 게이트웨이가 하는 일은 두 가지뿐이다
 *   1. 라우팅  : /api/auth/** → user-service, /api/posts/** → post-service   (application.yml)
 *   2. 인증    : 로그인 토큰을 검사하고, 통과하면 X-User-Id 헤더를 붙여 넘긴다   (AuthFilter)
 *   게이트웨이에는 회원·게시글 같은 업무 코드가 없다. 업무는 각 서비스가 한다.
 */
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
