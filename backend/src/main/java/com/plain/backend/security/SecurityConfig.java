package com.plain.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 이 클래스가 스프링 설정 클래스임을 명시합니다. 스프링이 구동될 때 이 클래스를 읽어 보안 설정을 적용합니다.
@EnableWebSecurity // 스프링 시큐리티 기능을 활성화합니다. 이 어노테이션이 있어야 아래 설정들이 작동합니다.
@RequiredArgsConstructor // final이 붙은 필드에 대해 자동으로 생성자를 만들어주는 롬복(Lombok) 기능입니다.
public class SecurityConfig {

    // 사용자의 HTTP 요청이 들어올 때마다 JWT 토큰을 검사하는 커스텀 필터입니다.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // [공부포인트] @Bean: 스프링 컨테이너가 직접 객체를 생성하고 관리하도록(IoC) 등록하는 어노테이션입니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 데이터베이스에 텍스트 그대로 저장하면 해킹 시 통째로 털리게 됩니다.
        // BCrypt라는 알고리즘을 사용해 복호화가 거의 불가능한 '해시(Hash)' 형태로 암호화해주는 객체입니다.
        return new BCryptPasswordEncoder();
    }

    // [공부포인트] SecurityFilterChain: 우리 서버로 들어오는 모든 요청이 통과해야 하는 '보안 검색대' 설정입니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF (Cross-Site Request Forgery) 공격 방어 기능 끄기
            // 전통적인 세션 방식에서는 필요하지만, 현대적인 REST API(JWT 토큰 기반)에서는 보통 세션을 사용하지 않으므로 꺼도 안전합니다.
            .csrf(csrf -> csrf.disable())
            
            // 2. 세션 정책 설정 (가장 중요)
            // [공부포인트] STATELESS: 서버가 사용자(클라이언트)의 상태를 기억하지 않겠다(세션을 만들지 않겠다)는 뜻입니다. 
            // 매번 요청이 올 때마다 헤더에 담긴 JWT 토큰을 확인해서 누군지 알아냅니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. API URL 경로별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // /api/auth/ 하위의 모든 경로는 로그인 없이도(permitAll) 접근 가능하게 열어둡니다. (예: 로그인, 회원가입 API)
                .requestMatchers("/api/auth/**").permitAll()
                // 개발 단계에서 H2 데이터베이스 콘솔 화면을 볼 수 있도록 권한 없이 열어둡니다.
                .requestMatchers("/h2-console/**").permitAll()
                // 그 외의 모든 요청(anyRequest)은 무조건 로그인을 통해 인증된(authenticated) 사용자만 통과시킵니다.
                .anyRequest().authenticated()
            )
            
            // 4. H2 콘솔 화면이 정상적으로 보이도록 frameOptions 설정을 끕니다. (보안상 막혀있는 것을 해제)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            
            // 5. JWT 필터 끼워넣기
            // 사용자가 아이디/비밀번호로 로그인하기(UsernamePasswordAuthenticationFilter) 전에,
            // 우리가 직접 만든 JWT 검증 필터(jwtAuthenticationFilter)를 먼저 거치도록 순서를 배치합니다.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 위에서 세팅한 내용들을 최종적으로 조립해서 스프링에게 건네줍니다.
    }
}
