package com.example.gateway.config;

import java.util.Objects;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

/**
 * Rate Limiter 설정 클래스
 * Spring Cloud Gateway의 RequestRateLimiter 필터가
 * "요청 횟수"를 계산할 때 사용할 "기준(Key)"을 정의합니다.
 */
@Configuration
public class RateLimitConfig {

  // 어떤 기준으로 사용자를 구분하여 토큰을 차감할지 결정
  @Bean
  KeyResolver userKeyResolver() {
    // exchange: 현재 들어온 HTTP 요청/응답 정보를 담고 있는 컨텍스트 (WebFlux)
    return exchange -> {
      // 헤더에서 X-User-ID를 찾아 Key로 사용 (로그인 사용자가 인증 필터를 거친 뒤 이 헤더가 있다고 가정한 상황)
      String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
      
      // 비로그인 상황이면(X-User-ID가 없으면) IP 주소를 Key로 사용
      // 로드밸런서나 프록시 뒤에 있다면, 실제 IP가 아닐 수 있음 (이런 경우 X-Forwarded-For 필요)
      if (userId == null) {
        userId = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
      }

      // WebFlux 기반이므로 일반 String이 아닌, 비동기 처리가 가능한 Mono<String>으로 감싸서 반환
      // Redis의 Key 뒷 부분에 붙이는 데이터 (예: request_rate_limiter.{userId})
      return Mono.just(userId);
    };
  }
}