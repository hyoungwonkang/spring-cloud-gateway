package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

  /**
   * 필터링 로직 핵심 메소드
   *
   * @param exchange ServerWebExchange: 요청(Request)과 응답(Response)을 모두 담고 있는 컨텍스트
   * @param chain    GatewayFilterChain: 다음 필터로 요청을 넘겨주는 체인 객체
   * @return Mono<Void>: 비동기 처리가 완료됨을 알리는 Reactor 객체
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    // Pre Filter: 요청 처리 전 실행 (요청 정보 기록 및 헤더 검사)
    log.info("Global Filter - Request Path: {}", exchange.getRequest().getPath());
    log.info("Global Filter - Request ID: {}", exchange.getRequest().getId());

    // 요청을 다음 필터(또는 서비스)로 진행
    return chain.filter(exchange)
	    .then(Mono.fromRunnable(() -> {
	      // Post Filter: 응답 처리 후 실행
	      log.info("Global Filter - Response Status: {}", exchange.getResponse().getStatusCode());
	    }));
  }

  /**
   * 필터 우선순위 설정
   * 
   * 숫자가 낮을수록(Low) 우선순위가 높습니다(High).
   * -1로 설정하면 다른 필터들보다 가장 먼저 실행되고, 가장 나중에 종료됩니다.
   * @return 우선순위 값 (HIGHEST_PRECEDENCE 근사값)
   */
  @Override
  public int getOrder() {
    return -1;
  }
}
