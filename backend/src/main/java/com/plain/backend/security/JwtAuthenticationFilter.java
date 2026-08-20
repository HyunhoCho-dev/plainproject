package com.plain.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// [공부포인트] OncePerRequestFilter: 클라이언트가 한 번 요청을 보낼 때, 이 필터도 딱 "한 번만" 작동하도록 보장해주는 클래스입니다.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 앞서 만든 토큰 감별소(JwtProvider)를 가져옵니다.
    private final JwtProvider jwtProvider;

    // doFilterInternal: 모든 HTTP 요청이 백엔드에 도착하기 전에 무조건 여기를 지나가게 됩니다. (보안 검색대)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청(Request)의 머리(Header) 부분에서 숨겨진 JWT 토큰을 쏙 빼옵니다.
        String token = resolveToken(request);

        // 2. 빼온 토큰이 존재하고, 그 토큰이 위조되지 않은 진짜 토큰(validateToken)이라면?
        if (token != null && jwtProvider.validateToken(token)) {
            // 3. 토큰을 뜯어서 주인이 누군지 확인하고, 스프링 시큐리티 전용 통행증(Authentication)을 발급받습니다.
            Authentication authentication = jwtProvider.getAuthentication(token);
            
            // 4. 발급받은 통행증을 SecurityContextHolder(스프링 시큐리티의 금고)에 안전하게 보관합니다.
            // 이렇게 보관해두면, 나중에 컨트롤러나 서비스에서 "이 사람 로그인한 사람 맞아?" 할 때 이 금고를 열어서 확인합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 검사가 끝났으니 "다음 필터나 컨트롤러로 지나가세요~" 하고 문을 열어줍니다.
        // 토큰이 없거나 가짜였어도 일단 지나가게 둡니다. (만약 인증이 필요한 경로라면 다음 단계에서 스프링이 자동으로 쫓아냅니다)
        filterChain.doFilter(request, response);
    }

    // [공부포인트] 헤더에서 토큰 추출하는 보조 메서드
    private String resolveToken(HttpServletRequest request) {
        // 클라이언트는 보통 토큰을 헤더에 담아서 보낼 때 키를 "Authorization"으로, 값을 "Bearer 토큰문자열..."로 보냅니다.
        String bearerToken = request.getHeader("Authorization");
        
        // 헤더에 값이 있고, 그 값이 "Bearer " 로 시작한다면
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 글자(7글자)를 싹둑 자르고 뒤에 있는 순수 토큰 문자열만 반환합니다.
            return bearerToken.substring(7);
        }
        return null; // 못 찾았으면 null 반환
    }
}
