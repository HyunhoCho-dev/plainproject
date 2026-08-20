package com.plain.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String smsAuthCode; // For SMS verification mock
}
