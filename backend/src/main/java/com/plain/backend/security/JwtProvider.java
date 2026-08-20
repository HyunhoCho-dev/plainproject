package com.plain.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

// [공부포인트] @Component: 스프링이 이 클래스를 객체(빈)로 생성해서 자동으로 관리하게 해주는 마법의 어노테이션입니다.
// 이 클래스는 JWT 토큰을 만들고, 뜯어보고, 가짜 토큰인지 검사하는 '토큰 발급소/감별소' 역할을 합니다.
@Component
public class JwtProvider {

    // 토큰에 도장을 찍을 때 쓰는 비밀 키 (절대 외부에 유출되면 안 됨!)
    private final SecretKey secretKey;
    // Access Token (짧은 수명)의 만료 시간
    private final long accessTokenValidityInMilliseconds;
    // Refresh Token (긴 수명)의 만료 시간
    private final long refreshTokenValidityInMilliseconds;

    // [공부포인트] @Value: application.yml 파일이나 환경변수에서 설정값을 가져옵니다.
    // ":" 뒤의 값은 설정파일에 값이 없을 때 사용할 '기본값'입니다.
    public JwtProvider(
            @Value("${jwt.secret:defaultSecretKeyWhichIsAtLeast32BytesLong12345!@#}") String secret,
            @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidityInSeconds, // 기본 1시간
            @Value("${jwt.refresh-token-validity-in-seconds:1209600}") long refreshTokenValidityInSeconds) { // 기본 14일
        
        // 입력받은 비밀 문자열을 암호화 알고리즘(HMAC-SHA)에 맞는 진짜 열쇠(SecretKey) 객체로 변환합니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        // 초 단위를 밀리초(ms) 단위로 변환합니다. (자바의 시간 계산은 기본적으로 밀리초 단위입니다)
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    // 1. 유저의 아이디(username)를 받아서 짧은 수명의 Access Token을 만들어주는 메서드
    public String createAccessToken(String username) {
        return createToken(username, accessTokenValidityInMilliseconds);
    }

    // 2. 유저의 아이디를 받아서 긴 수명의 Refresh Token을 만들어주는 메서드
    public String createRefreshToken(String username) {
        return createToken(username, refreshTokenValidityInMilliseconds);
    }

    // [공부포인트] 내부적으로 실제 JWT를 찍어내는 공장(로직)입니다. (private으로 숨김)
    private String createToken(String username, long validityInMilliseconds) {
        Date now = new Date();
        // 지금 시간(now) + 유효시간을 더해서 '언제 만료될지(validity)' 시간을 계산합니다.
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)       // 페이로드(내용물): 이 토큰의 주인이 누구인지(아이디) 적습니다.
                .setIssuedAt(now)           // 페이로드: 토큰이 발급된 시간을 적습니다.
                .setExpiration(validity)    // 페이로드: 토큰이 만료될 시간을 적습니다.
                .signWith(secretKey, SignatureAlgorithm.HS256) // 서명: 우리가 가진 비밀 키로 도장을 쾅 찍습니다. (위조 방지)
                .compact();                 // 이 모든 정보를 압축해서 하나의 문자열(Token)로 만듭니다.
    }

    // 3. 토큰을 뜯어서 그 안에 있는 사용자 정보를 스프링 시큐리티용 '인증 객체(Authentication)'로 바꿔주는 메서드
    public Authentication getAuthentication(String token) {
        // 비밀 키를 사용해 토큰의 봉투를 뜯고 안에 적힌 내용(Claims)을 읽어옵니다.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 토큰의 Subject에 적어뒀던 '사용자 아이디(username)'를 꺼냅니다.
        // 임시로 이 사용자는 "ROLE_USER" 권한을 가졌다고 리스트에 담아둡니다.
        User principal = new User(claims.getSubject(), "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // 스프링 시큐리티에게 "이 토큰 가져온 사람 정상 인증된 사람이야~" 하고 알려주는 통행증 객체를 만들어 반환합니다.
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // 4. 클라이언트가 보낸 토큰이 유효한지(가짜가 아닌지, 유효기간이 안 지났는지) 검사하는 메서드
    public boolean validateToken(String token) {
        try {
            // 토큰 봉투 뜯기를 시도해봅니다. 도장(서명)이 다르거나 만료되었다면 여기서 에러(Exception)가 터집니다.
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true; // 에러 안나면 진짜 토큰!
        } catch (JwtException | IllegalArgumentException e) {
            // 유효기간 만료, 위조된 토큰, 빈 토큰 등등 문제가 있으면 false를 반환합니다.
            return false; 
        }
    }
}
