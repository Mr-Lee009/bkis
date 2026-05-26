package vn.edu.bkis.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.common.BusinessException;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.model.PasswordResetToken;
import vn.edu.bkis.model.User;
import vn.edu.bkis.repository.PasswordResetTokenRepository;
import vn.edu.bkis.repository.UserRepository;

@Service
public class PasswordResetService {
    private static final int TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    // Khoi tao service xu ly luong quen mat khau va dat lai mat khau bang token.
    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Tao token reset password neu tai khoan ton tai, tra Optional rong de tranh lo thong tin user.
    @Transactional
    public Optional<String> requestReset(String account) {
        if (account == null || account.isBlank()) {
            return Optional.empty();
        }
        Optional<User> user = findUserByAccount(account.trim());
        if (user.isEmpty()) {
            return Optional.empty();
        }
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.get().getId());
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES));
        resetToken.setCreatedBy("password-reset");
        resetToken.setUpdatedBy("password-reset");
        tokenRepository.save(resetToken);
        return Optional.of(resetToken.getToken());
    }

    // Kiem tra token co ton tai, chua dung va chua het han hay khong.
    public boolean isValidToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(this::isUsable)
                .isPresent();
    }

    // Dat lai mat khau moi va danh dau token da su dung.
    @Transactional
    public void resetPassword(String token, String password, String confirmPassword) {
        validatePassword(password, confirmPassword);
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .filter(this::isUsable)
                .orElseThrow(() -> new BusinessException(MessageCode.E0019));
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BusinessException(MessageCode.E0021));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        user.setLocked(false);
        user.setUpdatedBy("password-reset");
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        resetToken.setUpdatedBy("password-reset");
        tokenRepository.save(resetToken);
    }

    // Tim user theo username truoc, neu khong co thi tim theo email.
    private Optional<User> findUserByAccount(String account) {
        return userRepository.findByUsername(account)
                .or(() -> userRepository.findByEmail(account));
    }

    // Kiem tra token con dung duoc trong luong reset password.
    private boolean isUsable(PasswordResetToken token) {
        return token.getUsedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    // Kiem tra mat khau moi du dieu kien toi thieu va trung xac nhan.
    private void validatePassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new BusinessException(MessageCode.E0011, 8);
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(MessageCode.E0013);
        }
    }
}
