package com.plain.backend.api.account;

import com.plain.backend.user.User;

/** 로그인·회원가입 화면과 주고받는 데이터입니다. 비밀번호는 응답에 절대 담지 않습니다. */
public final class AccountDtos {

    private AccountDtos() {}

    public record SignupRequest(String username, String email, String password) {}

    public record LoginRequest(String username, String password) {}

    /** 회원탈퇴. 본인 확인을 위해 비밀번호를 다시 받습니다. */
    public record WithdrawRequest(Long userId, String password) {}

    public record UserResponse(Long id, String username, String email) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        }
    }
}
