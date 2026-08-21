package com.plain.backend.api.account;

import com.plain.backend.api.focus.FocusRepository;
import com.plain.backend.api.plan.StudyPlanRepository;
import com.plain.backend.api.stats.StatsRepository;
import com.plain.backend.monitoring.NotificationRepository;
import com.plain.backend.session.FocusSession;
import com.plain.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

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
    private final FocusRepository focusRepository;
    private final StatsRepository statsRepository;
    private final NotificationRepository notificationRepository;
    private final StudyPlanRepository studyPlanRepository;

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
     * 회원탈퇴. 계정과 함께 이 사용자가 남긴 기록을 모두 지웁니다.
     *
     * 지우는 순서가 중요합니다. 알림·방해 기록은 세션을 가리키고 세션은 사용자를 가리키므로,
     * 바깥쪽(알림)부터 지워야 외래 키 제약에 걸리지 않습니다.
     * 계획 블록은 StudyPlan에 cascade가 걸려 있어 계획을 지우면 함께 사라집니다.
     */
    @Transactional
    public void withdraw(AccountDtos.WithdrawRequest request) {
        if (request == null || request.userId() == null) {
            throw new IllegalArgumentException("탈퇴할 계정을 찾지 못했습니다.");
        }
        User user = accountRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        // 본인 확인. 화면에서 비밀번호를 다시 받습니다.
        if (!matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다");
        }

        List<FocusSession> sessions = focusRepository.findByUserIdOrderByStartTimeDesc(user.getId());
        for (FocusSession session : sessions) {
            notificationRepository.deleteAll(notificationRepository.findByFocusSessionId(session.getId()));
        }
        statsRepository.deleteAll(statsRepository.findByFocusSessionUserId(user.getId()));
        focusRepository.deleteAll(sessions);
        studyPlanRepository.deleteAll(studyPlanRepository.findByUserId(user.getId()));
        accountRepository.delete(user);
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
