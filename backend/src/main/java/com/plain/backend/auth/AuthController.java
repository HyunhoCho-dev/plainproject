package com.plain.backend.auth;

import com.plain.backend.auth.dto.LoginRequest;
import com.plain.backend.auth.dto.SignupRequest;
import com.plain.backend.auth.dto.SocialAuthRequest;
import com.plain.backend.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// [공부포인트] @RestController: 이 클래스는 클라이언트(프론트엔드)의 요청(URL)을 받아서 JSON 형태로 데이터를 응답해주는 '창구 직원' 역할을 합니다.
@RestController
// 이 컨트롤러 안에 있는 모든 주소(URL) 앞에 기본적으로 "/api/auth" 를 붙여줍니다. (공통 경로)
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // 창구 직원은 손님이 오면 실제로 복잡한 일을 처리할 '실무 담당자(AuthService)'에게 일을 넘깁니다.
    private final AuthService authService;

    // [소셜 로그인] POST /api/auth/social/{provider}
    // 프론트엔드가 구글/카카오에서 받아온 인증코드(authCode)를 던져주면 처리합니다.
    @PostMapping("/social/{provider}")
    public ResponseEntity<TokenResponse> socialLogin(@PathVariable String provider,
                                                     @RequestBody SocialAuthRequest request) {
        // 실무 담당자(Service)에게 소셜 이름과 코드를 주면서 처리를 지시합니다.
        TokenResponse tokenResponse = authService.socialLogin(provider, request.getAuthCode());
        // 성공(200 OK)과 함께 발급된 토큰 2개를 프론트엔드에 돌려줍니다.
        return ResponseEntity.ok(tokenResponse);
    }

    // [자체 회원가입] POST /api/auth/signup
    // 사용자가 입력한 아이디, 비번, 전화번호 등을 받습니다.
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody SignupRequest request) {
        TokenResponse tokenResponse = authService.signup(request);
        return ResponseEntity.ok(tokenResponse);
    }

    // [자체 로그인] POST /api/auth/login
    // 아이디와 비밀번호를 받아서 로그인을 시도합니다.
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    // [토큰 재발급] POST /api/auth/refresh
    // 수명이 짧은 Access 토큰이 죽었을 때, 수명이 긴 Refresh 토큰을 보여주며 새 Access 토큰을 발급받습니다.
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader("Authorization") String bearerToken) {
        // 헤더에 토큰이 제대로 담겨있는지 확인합니다.
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String refreshToken = bearerToken.substring(7); // "Bearer " 잘라내기
            TokenResponse tokenResponse = authService.refresh(refreshToken);
            return ResponseEntity.ok(tokenResponse); // 새 토큰 세트 반환!
        }
        // 헤더가 이상하면 400 Bad Request 에러를 던집니다.
        return ResponseEntity.badRequest().build();
    }

    // [회원 탈퇴] DELETE /api/auth/withdraw
    // 현재 로그인된 사용자의 정보를 데이터베이스에서 삭제합니다.
    @DeleteMapping("/withdraw")
    // 매개변수인 Authentication은 앞서 우리가 만든 JwtAuthenticationFilter에서 넣어준 '통행증'입니다.
    public ResponseEntity<Void> withdraw(Authentication authentication) {
        // 로그인 안 한 사람이 탈퇴하겠다고 떼쓰는 경우 차단 (401 에러)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        // 통행증에 적힌 내 이름(아이디)를 알아냅니다.
        String username = authentication.getName();
        authService.withdraw(username); // 탈퇴 처리 완료!
        return ResponseEntity.ok().build(); // 빈 응답과 200 OK
    }

    // [로그아웃] POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // [공부포인트] JWT는 STATELESS(무상태)이기 때문에 서버는 로그아웃을 해줄 필요가 없습니다.
        // 클라이언트(프론트)가 자기 폰/브라우저에 저장해둔 토큰을 휴지통에 버리면 그게 로그아웃입니다!
        // 서버에서는 "응, 수고했어~" 하고 200 OK만 내려줍니다.
        return ResponseEntity.ok().build();
    }
}
