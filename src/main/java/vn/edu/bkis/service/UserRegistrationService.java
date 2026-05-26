package vn.edu.bkis.service;

import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bkis.common.BusinessException;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.dto.RegisterAccountFormDto;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.UserRepository;

@Service
public class UserRegistrationService {
    private static final String DEFAULT_PROFILE_PICTURE = "/img/testimonial-1.jpg";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Khoi tao service dang ky tai khoan nguoi dung voi repository va bo ma hoa mat khau.
    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Tao tai khoan hoc vien moi sau khi kiem tra du lieu dau vao va chong trung username/email.
    @Transactional
    public void register(RegisterAccountFormDto form) {
        String username = required(form.getUsername(), MessageCode.E0001);
        String fullName = required(form.getFullName(), MessageCode.E0003);
        String email = required(form.getEmail(), MessageCode.E0005).toLowerCase(Locale.ROOT);
        String password = required(form.getPassword(), MessageCode.E0007);
        String confirmPassword = required(form.getConfirmPassword(), MessageCode.E0009);

        /*
         * Kiem tra mat khau va tai khoan trung truoc khi tao user moi.
         * User tu dang ky luon la STUDENT de khong tu cap quyen quan tri.
         */
        validatePassword(password, confirmPassword);
        validateUniqueAccount(username, email);

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.STUDENT);
        user.setBio("Self-registered student account.");
        user.setProfilePictureUrl(DEFAULT_PROFILE_PICTURE);
        user.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        user.setLocked(false);
        user.setCreatedBy("self-register");
        user.setUpdatedBy("self-register");
        userRepository.save(user);
    }

    // Kiem tra username/email chua ton tai trong he thong.
    private void validateUniqueAccount(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(MessageCode.E0015, username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(MessageCode.E0017, email);
        }
    }

    // Kiem tra mat khau du dai va khop voi o xac nhan.
    private void validatePassword(String password, String confirmPassword) {
        if (password.length() < 8) {
            throw new BusinessException(MessageCode.E0011, 8);
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(MessageCode.E0013);
        }
    }

    // Chuan hoa du lieu bat buoc va bao loi bang ma message khi nguoi dung bo trong.
    private String required(String value, String messageCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(messageCode);
        }
        return value.trim();
    }
}
