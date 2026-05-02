package vn.edu.bkis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.bkis.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Tìm token reset password còn lưu trong hệ thống để xác thực yêu cầu đổi mật khẩu.
    Optional<PasswordResetToken> findByToken(String token);
}
