package com.plain.backend.api.account;

import com.plain.backend.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AccountDtos.UserResponse>> signup(
            @RequestBody AccountDtos.SignupRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccountDtos.UserResponse>> login(
            @RequestBody AccountDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.login(request)));
    }

    /** 시연용. 소셜 로그인 자리에서 가입 없이 화면 흐름을 이어보기 위해 씁니다. */
    @PostMapping("/demo")
    public ResponseEntity<ApiResponse<AccountDtos.UserResponse>> demo() {
        return ResponseEntity.ok(ApiResponse.ok(accountService.findOrCreateDemoUser()));
    }
}
