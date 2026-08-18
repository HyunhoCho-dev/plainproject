package com.plain.backend.api.account;

import com.plain.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 회원가입과 로그인입니다.
 *
 * 프로토타입 단계라 토큰이나 세션은 발급하지 않고 userId만 돌려줍니다.
 * 화면은 이 userId를 들고 다니며 계획·세션 API를 호출합니다.
 *
 * [나중에 바꿀 것] 비밀번호는 salt + SHA-256으로 저장합니다.
 * build.gradle을 건드리지 않으려고 표준 라이브러리만 썼기 때문입니다.
 * spring-security-crypto 의존성을 추가할 수 있게 되면 BCrypt로 바꾸는 편이 안전합니다.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;

    @Transactional
    public AccountDtos.UserResponse signup(AccountDtos.SignupRequest request) {
        String username = require(request.username(), "아이디");
        String password = require(request.password(), "비밀번호");
        // 회원가입 화면에 이메일 칸이 없어서 아이디로 임시 주소를 만듭니다.
        // User 엔티티가 이메일을 필수로 요구하기 때문입니다.
        String email = request.email() == null || request.email().isBlank()
                ? username + "@plain.local"
                : request.email();

        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User saved = accountRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(hash(password))
                .build());

        return AccountDtos.UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountDtos.UserResponse login(AccountDtos.LoginRequest request) {
        // 아이디와 비밀번호 중 무엇이 틀렸는지는 구분해서 알려주지 않습니다 (로그인 화면 문구와 동일).
        User user = accountRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다"));

        if (!matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다");
        }
        return AccountDtos.UserResponse.from(user);
    }

    /**
     * 시연용 계정입니다.
     * 소셜 로그인처럼 아직 실제 연동이 없는 자리에서 가입 절차 없이 화면 흐름을 끝까지 보기 위해 씁니다.
     */
    @Transactional
    public AccountDtos.UserResponse findOrCreateDemoUser() {
        return accountRepository.findByUsername("demo")
                .map(AccountDtos.UserResponse::from)
                .orElseGet(() -> signup(new AccountDtos.SignupRequest("demo", "demo@plain.local", "demo1234")));
    }

    /* ── 비밀번호 저장 ──────────────────────────────── */

    /** "salt$해시" 형태로 만듭니다. 같은 비밀번호라도 사용자마다 다른 값이 저장됩니다. */
    private String hash(String rawPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        return encodedSalt + "$" + sha256(encodedSalt + rawPassword);
    }

    private boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) return false;
        int separator = stored.indexOf('$');
        if (separator < 0) return false;
        String salt = stored.substring(0, separator);
        return stored.substring(separator + 1).equals(sha256(salt + rawPassword));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("비밀번호를 처리하지 못했습니다.", exception);
        }
    }

    private String require(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을(를) 입력해주세요.");
        }
        return value.trim();
    }
}
