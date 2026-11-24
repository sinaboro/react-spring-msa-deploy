package com.example.gateway;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

/**
 * GatewayErrorHandler - 게이트웨이에서 난 오류를 { "detail": "..." } JSON 으로 바꿔 준다
 *
 * [MSA 개념] 서비스 하나가 꺼져도 정문은 친절하게
 *   user-service 가 꺼진 채로 로그인하면, 게이트웨이는 전달할 곳에 연결하지 못한다(ConnectException).
 *   이 파일이 없으면 Spring 기본 모양 {"timestamp":..., "status":500, ...} 이 나가서
 *   React 화면에는 이유 없는 "요청 실패 (500)" 만 보인다.
 *   → 503(Service Unavailable, 서비스를 쓸 수 없음) + 한국어 이유로 바꿔 준다.
 *
 *   상황                                   상태 코드
 *   전달할 서비스에 연결 못 함 (꺼져 있음)      503 "서비스에 연결할 수 없습니다 ..."
 *   라우팅 규칙에 없는 주소 (/api/nothing)     404 "없는 주소입니다"
 *   그 밖의 오류                            500 "게이트웨이 오류가 났습니다"
 *
 * @Order(-2) : Spring 이 기본으로 넣어 둔 오류 처리기(-1)보다 먼저 실행되게 한다.
 */
@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();
		if (response.isCommitted()) { // 응답을 이미 보내기 시작했으면 바꿀 수 없다
			return Mono.error(ex);
		}

		HttpStatus status;
		String message;
		if (isConnectionRefused(ex)) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			message = "서비스에 연결할 수 없습니다. 서비스가 켜져 있는지 확인하세요";
		} else if (ex instanceof ResponseStatusException e && e.getStatusCode().value() == 404) {
			status = HttpStatus.NOT_FOUND;
			message = "없는 주소입니다";
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			message = "게이트웨이 오류가 났습니다";
		}

		response.setStatusCode(status);
		response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		byte[] body = ("{\"detail\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
		return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
	}

	/**
	 * 오류의 원인을 거슬러 올라가며 "서비스에 닿지 못했다" 는 예외가 있는지 찾는다
	 *   ConnectException        : 연결 거부 (서비스가 꺼져 있음)
	 *   PrematureCloseException : 연결은 됐지만 응답 전에 끊김 (서비스가 꺼지는 중이거나, Docker 가 포트만 열어 둔 경우)
	 */
	private boolean isConnectionRefused(Throwable ex) {
		for (Throwable t = ex; t != null; t = t.getCause()) {
			if (t instanceof ConnectException || t instanceof PrematureCloseException) {
				return true;
			}
		}
		return false;
	}
}
