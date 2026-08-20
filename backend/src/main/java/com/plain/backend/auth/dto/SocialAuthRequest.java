package com.plain.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialAuthRequest {
    private String authCode; // The authorization code returned by OAuth provider
}
