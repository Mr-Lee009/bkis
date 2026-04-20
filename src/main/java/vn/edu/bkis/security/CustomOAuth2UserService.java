package vn.edu.bkis.security;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.edu.bkis.model.AuthProvider;
import vn.edu.bkis.model.User;
import vn.edu.bkis.model.UserOAuthAccount;
import vn.edu.bkis.model.UserRole;
import vn.edu.bkis.repository.UserOAuthAccountRepository;
import vn.edu.bkis.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    // Khởi tạo service xử lý dữ liệu người dùng nhận về từ Google hoặc Facebook.
    public CustomOAuth2UserService(UserRepository userRepository,
                                   UserOAuthAccountRepository userOAuthAccountRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Tải thông tin người dùng từ provider, link với user local và trả principal có quyền theo hệ thống nội bộ.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        AuthProvider provider = resolveProvider(userRequest);
        OAuth2UserProfile profile = extractUserProfile(provider, oauth2User.getAttributes());
        User user = resolveLocalUser(provider, profile);
        return new CustomOAuth2User(user, oauth2User.getAttributes());
    }

    // Xác định provider hiện tại từ registrationId đã cấu hình trong Spring Security.
    private AuthProvider resolveProvider(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if ("google".equalsIgnoreCase(registrationId)) {
            return AuthProvider.GOOGLE;
        }
        if ("facebook".equalsIgnoreCase(registrationId)) {
            return AuthProvider.FACEBOOK;
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "Nhà cung cấp SSO hiện tại chưa được hỗ trợ.");
    }

    // Chuẩn hóa dữ liệu profile từ từng provider về cùng một cấu trúc xử lý trong hệ thống.
    private OAuth2UserProfile extractUserProfile(AuthProvider provider, Map<String, Object> attributes) {
        if (provider == AuthProvider.GOOGLE) {
            return new OAuth2UserProfile(
                    requiredString(attributes, "sub"),
                    requiredString(attributes, "email"),
                    optionalString(attributes, "name"),
                    optionalString(attributes, "picture"));
        }
        if (provider == AuthProvider.FACEBOOK) {
            return new OAuth2UserProfile(
                    requiredString(attributes, "id"),
                    requiredString(attributes, "email"),
                    optionalString(attributes, "name"),
                    extractFacebookPicture(attributes));
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "Không thể chuẩn hóa dữ liệu từ provider hiện tại.");
    }

    // Tìm user local đã liên kết hoặc tạo mới tài khoản local nếu đây là lần đăng nhập SSO đầu tiên.
    private User resolveLocalUser(AuthProvider provider, OAuth2UserProfile profile) {
        Optional<UserOAuthAccount> existingAccount =
                userOAuthAccountRepository.findByProviderAndProviderUserId(provider, profile.providerUserId());

        if (existingAccount.isPresent()) {
            User linkedUser = userRepository.findById(existingAccount.get().getUserId())
                    .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error("linked_user_not_found"),
                            "Không tìm thấy user local đã liên kết với tài khoản SSO."));
            validateUserCanLogin(linkedUser);
            syncLinkedAccount(existingAccount.get(), profile);
            return linkedUser;
        }

        User user = userRepository.findByEmail(profile.email())
                .map(existingUser -> {
                    validateUserCanLogin(existingUser);
                    return existingUser;
                })
                .orElseGet(() -> createLocalUser(profile));

        createLinkedAccount(user, provider, profile);
        return user;
    }

    // Kiểm tra trạng thái tài khoản local trước khi cho phép hoàn tất đăng nhập qua SSO.
    private void validateUserCanLogin(User user) {
        if (Boolean.TRUE.equals(user.getLocked())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("user_locked"),
                    "Tài khoản hiện đang bị khóa nên không thể đăng nhập bằng SSO.");
        }
    }

    // Tạo mới user local để hệ thống vẫn quản lý role và dữ liệu nội bộ theo mô hình hiện tại.
    private User createLocalUser(OAuth2UserProfile profile) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(generateUniqueUsername(profile.email(), profile.displayName()));
        user.setFullName(resolveDisplayName(profile));
        user.setBio("Tài khoản được tạo tự động từ đăng nhập SSO.");
        user.setProfilePictureUrl(profile.pictureUrl());
        user.setEmail(profile.email());
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(UserRole.STUDENT);
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        return userRepository.save(user);
    }

    // Tạo bản ghi liên kết giữa user local và tài khoản OAuth2 của provider.
    private void createLinkedAccount(User user, AuthProvider provider, OAuth2UserProfile profile) {
        UserOAuthAccount account = new UserOAuthAccount();
        account.setUserId(user.getId());
        account.setProvider(provider);
        account.setProviderUserId(profile.providerUserId());
        account.setEmail(profile.email());
        account.setDisplayName(resolveDisplayName(profile));
        account.setProfilePictureUrl(profile.pictureUrl());
        userOAuthAccountRepository.save(account);
    }

    // Đồng bộ nhanh các thông tin hiển thị của tài khoản SSO đã liên kết để thuận tiện cho lần đăng nhập sau.
    private void syncLinkedAccount(UserOAuthAccount account, OAuth2UserProfile profile) {
        account.setEmail(profile.email());
        account.setDisplayName(resolveDisplayName(profile));
        account.setProfilePictureUrl(profile.pictureUrl());
        userOAuthAccountRepository.save(account);
    }

    // Sinh username local duy nhất từ email hoặc tên hiển thị để tránh xung đột khi tạo user mới.
    private String generateUniqueUsername(String email, String displayName) {
        String base = sanitizeUsernameBase(email, displayName);
        String candidate = base;
        int index = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + index;
            index++;
        }
        return candidate;
    }

    // Chuẩn hóa phần gốc của username để phù hợp với quy tắc đặt tên của hệ thống hiện tại.
    private String sanitizeUsernameBase(String email, String displayName) {
        String source = email != null && email.contains("@")
                ? email.substring(0, email.indexOf('@'))
                : displayName;
        String normalized = source == null ? "user" : source.trim().toLowerCase()
                .replaceAll("[^a-z0-9._-]", "");
        return normalized.isBlank() ? "user" : normalized;
    }

    // Trả về tên hiển thị hợp lệ cho user local nếu provider không gửi đủ dữ liệu tên.
    private String resolveDisplayName(OAuth2UserProfile profile) {
        if (profile.displayName() != null && !profile.displayName().isBlank()) {
            return profile.displayName();
        }
        if (profile.email() != null && profile.email().contains("@")) {
            return profile.email().substring(0, profile.email().indexOf('@'));
        }
        return "BKIS User";
    }

    // Lấy giá trị bắt buộc từ attribute và chặn đăng nhập nếu provider không trả về dữ liệu cần thiết.
    private String requiredString(Map<String, Object> attributes, String key) {
        String value = optionalString(attributes, key);
        if (value == null || value.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_attribute"),
                    "Thiếu thông tin bắt buộc từ provider: " + key);
        }
        return value;
    }

    // Lấy giá trị chuỗi tùy chọn từ tập attributes nhận về từ provider.
    private String optionalString(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }

    // Đọc URL ảnh đại diện từ payload Facebook nếu provider trả về cấu trúc ảnh lồng nhau.
    @SuppressWarnings("unchecked")
    private String extractFacebookPicture(Map<String, Object> attributes) {
        Object picture = attributes.get("picture");
        if (!(picture instanceof Map<?, ?> pictureMap)) {
            return null;
        }
        Object data = pictureMap.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return null;
        }
        Object url = dataMap.get("url");
        return url == null ? null : String.valueOf(url);
    }

    private record OAuth2UserProfile(String providerUserId, String email, String displayName, String pictureUrl) {
    }
}
