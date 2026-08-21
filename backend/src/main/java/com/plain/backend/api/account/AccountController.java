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

    /** 회원탈퇴. 계정과 계획·세션·기록을 모두 지웁니다. 되돌릴 수 없습니다. */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @RequestBody AccountDtos.WithdrawRequest request) {
        accountService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
