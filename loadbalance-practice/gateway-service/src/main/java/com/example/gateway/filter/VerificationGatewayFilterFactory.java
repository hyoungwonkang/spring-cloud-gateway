package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 인증 검증 필터 (VerificationGatewayFilterFactory Filter)
 * 
 * 특정 라우트(예: /payments/**)에 진입하려는 요청에 대해
 * 허가된 사용자(키 값을 가진 사용자)인지 확인합니다.
 * AbstractGatewayFilterFactory<Config>를 상속받아야 application.yml에서 이름으로 필터를 적용할 수 있습니다.
 */
@Slf4j
@Component
public class VerificationGatewayFilterFactory extends AbstractGatewayFilterFactory<VerificationGatewayFilterFactory.Config> {
  
  // application.yml에서 인자(args)를 넘겨받을 때 사용
  public static class Config { }

  // AbstractGatewayFilterFactory에 설정을 넘겨주면 스프링이 설정값을 매핑하여 사용
  public VerificationGatewayFilterFactory() {
    super(Config.class);
  }

  // 인증에 실패했을 때, 백엔드 서비스로 요청을 넘기지 않고 즉시 클라이언트에게 응답
  private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
    exchange.getResponse().setStatusCode(status);
    return exchange.getResponse().setComplete();  // 더 이상 뒤로 진행하지 않고 끝냄
  }

  // 실제 요청이 들어오면 동작하는 코드
  @Override
  public GatewayFilter apply(Config config) {
    // exchange: ServerWebExchange(요청과 응답을 하나로 묶은 컨테이너)
    return (exchange, chain) -> {
      // 요청 헤더(Header)에서 인증 키(X-Secret-Key) 꺼내기
      String secretKey = exchange.getRequest().getHeaders().getFirst("X-Secret-Key");

      // 인증 키 검증 예시 코드
      if (secretKey == null || !secretKey.equals("secret1234")) {
        log.warn("Invalid access attempt. Key: {}", secretKey);
        return onError(exchange, HttpStatus.UNAUTHORIZED);  // 권한 없음
      }

      // 성공 시 다음 단계로 진행
      log.info("Verification Successful.");
      return chain.filter(exchange);
    };
  }
}
