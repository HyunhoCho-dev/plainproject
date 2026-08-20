package com.plain.backend.auth;

import com.plain.backend.auth.dto.LoginRequest;
import com.plain.backend.auth.dto.SignupRequest;
import com.plain.backend.auth.dto.TokenResponse;
import com.plain.backend.security.JwtProvider;
import com.plain.backend.user.User;
import com.plain.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [공부포인트] @Service: 컨트롤러가 받아온 요청을 바탕으로, 실제 '핵심 비즈니스 로직(일처리)'을 수행하는 두뇌 역할입니다.
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository; // 데이터베이스 접근용 창고
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
    private final JwtProvider jwtProvider; // JWT 토큰 발급 도구

    // [공부포인트] @Transactional: 이 메서드 안에서 일어나는 데이터베이스 작업은 
    // 하나라도 실패하면 모두 없던 일(Rollback)로 되돌려줍니다. 안전성을 위해 사용합니다.
    @Transactional
    public TokenResponse signup(SignupRequest request) {
        // 1. 이미 누군가 쓰고 있는 아이디인지 중복 검사합니다.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        // 2. 이미 등록된 전화번호인지 중복 검사합니다.
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        // [SMS 인증 모의처리] 실제로는 여기서 request.getSmsAuthCode()가 맞는지 확인하는 로직이 들어갑니다.

        // 3. User 엔티티(데이터베이스 저장용 껍데기)를 만듭니다.
        User user = User.builder()
                .username(request.getUsername())
                // 사용자가 입력한 비밀번호(1234)를 복호화 불가한 외계어(dsflk23j4...)로 암호화해서 넣습니다!
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .loginType("LOCAL") // 이 사람은 우리 앱으로 자체 가입한 사람(LOCAL)이라고 표시합니다.
                .build();

        // 4. 데이터베이스에 저장!
        userRepository.save(user);

        // 5. 가입 성공 기념으로 즉시 로그인 상태로 만들어주기 위해 토큰 2개를 발급해서 줍니다.
        String accessToken = jwtProvider.createAccessToken(user.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        return new TokenResponse(accessToken, refreshToken);
    }

    // @Transactional(readOnly = true) : 이 메서드는 데이터를 읽기만 하니까 성능 최적화를 부탁한다는 뜻입니다.
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        // 1. 아이디로 데이터베이스를 뒤져서 회원을 찾습니다. 없으면 에러 던집니다.
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // 2. 사용자가 입력한 쌩 비밀번호와, DB에 저장된 암호화된 비밀번호가 '일치'하는지 확인합니다.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 3. 아이디/비밀번호 통과! 통행증(토큰) 2개를 발급합니다.
        String accessToken = jwtProvider.createAccessToken(user.getUsername());
        String refreshToken = jwtProvider.createRefreshToken(user.getUsername());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse socialLogin(String provider, String authCode) {
        // [공부포인트] 소셜 로그인의 흐름 (Mocking)
        // 실제로는: 구글/카카오에게 저 authCode를 넘겨주고, 구글/카카오로부터 '진짜 프로필 정보'를 받아와야 합니다.
        // 현재는 구현을 흉내(Mocking)내기 위해 가짜 데이터를 만듭니다.
        String mockSocialId = provider + "_" + authCode; // 예: GOOGLE_abcd123
        String mockNickname = provider + "User";

        // 데이터베이스에서 이미 가입한 적이 있는지 소셜 ID로 찾아봅니다.
        User user = userRepository.findBySocialProviderAndSocialId(provider.toUpperCase(), mockSocialId)
                .orElseGet(() -> { // 만약 처음 로그인하는 거라면? (회원가입 자동 진행)
                    User newUser = User.builder()
                            .nickname(mockNickname)
                            .loginType("SOCIAL") // 소셜 유저로 표시
                            .socialProvider(provider.toUpperCase())
                            .socialId(mockSocialId)
                            .password(passwordEncoder.encode("SOCIAL_DUMMY_PASSWORD")) // 의미없는 쓰레기 비밀번호
                            .build();
                    return userRepository.save(newUser);
                });

        // 사용자 식별을 위해 일반 아이디가 없으면 소셜 고유 ID를 주어로 사용합니다.
        String subject = user.getUsername() != null ? user.getUsername() : user.getSocialId();
        
        // 로그인 완료 토큰 발급!
        String accessToken = jwtProvider.createAccessToken(subject);
        String refreshToken = jwtProvider.createRefreshToken(subject);

        return new TokenResponse(accessToken, refreshToken);
    }

    // Refresh Token을 가져오면, 새로운 Access Token을 발급해주는 기능 (자동 로그인 유지 용도)
    public TokenResponse refresh(String refreshToken) {
        // 1. 유효기간 지나지 않은 진짜 Refresh 토큰인지 검사합니다.
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        // 2. 토큰을 뜯어서 누구 토큰인지 확인합니다.
        String username = jwtProvider.getAuthentication(refreshToken).getName();
        
        // 3. 신선한 Access Token 발급
        String newAccessToken = jwtProvider.createAccessToken(username);
        // [보안강화] Refresh Token도 새로 발급해서 탈취 위험을 낮춥니다 (Rotation 기법)
        String newRefreshToken = jwtProvider.createRefreshToken(username);
        
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void withdraw(String username) {
        // 데이터베이스에서 아이디로 사용자를 찾아냅니다.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // 데이터베이스에서 해당 회원의 정보를 영구 삭제합니다.
        userRepository.delete(user);
    }
}
