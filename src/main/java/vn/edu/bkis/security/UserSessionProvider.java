package vn.edu.bkis.security;

import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.edu.bkis.model.AuthProvider;
import vn.edu.bkis.model.User;

@Component
public class UserSessionProvider {

    /**
     * Lấy thông tin phiên người dùng hiện tại từ SecurityContext nếu đã đăng nhập.
     *
     * @return {@link Optional} chứa {@link UserSession} khi có principal hợp lệ, ngược lại trả về rỗng
     */
    public Optional<UserSession> getCurrentUserSessionOptional() {
        // Step 1: lấy authentication hiện tại từ SecurityContext và loại bỏ các phiên anonymous.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Step 2: map principal hiện tại về UserSession theo từng loại principal mà ứng dụng đang hỗ trợ.
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return Optional.of(buildUserSession(customUserDetails.getUser(), AuthProvider.LOCAL));
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return Optional.of(buildUserSession(customOAuth2User.getUser(), resolveProvider(customOAuth2User)));
        }

        // Step 3: trả về rỗng nếu principal không thuộc các loại principal nội bộ đã chuẩn hóa.
        return Optional.empty();
    }

    /**
     * Lấy thông tin phiên người dùng hiện tại và ném lỗi nếu chưa có đăng nhập hợp lệ.
     *
     * @return {@link UserSession} của người dùng hiện tại trong phiên bảo mật đang hoạt động
     * @throws IllegalStateException ném ra khi service được gọi ở ngữ cảnh chưa có người dùng đăng nhập
     */
    public UserSession getCurrentUserSession() {
        // Step 1: đọc UserSession từ SecurityContext hiện tại.
        // Step 2: chặn sớm nếu service đang bị gọi ở ngữ cảnh không có principal hợp lệ.
        return getCurrentUserSessionOptional()
                .orElseThrow(() -> new IllegalStateException("Current authenticated user is required."));
    }

    /**
     * Lấy định danh audit ưu tiên dùng cho các field createdBy và updatedBy.
     *
     * @return username của người dùng hiện tại nếu đã đăng nhập, ngược lại trả về `system`
     */
    public String getCurrentAuditActor() {
        // Step 1: lấy UserSession hiện tại nếu có người dùng đăng nhập.
        // Step 2: ưu tiên username để ghi audit vì ổn định và dễ truy vết hơn full name.
        return getCurrentUserSessionOptional()
                .map(UserSession::username)
                .filter(username -> username != null && !username.isBlank())
                .orElse("system");
    }

    /**
     * Chuyển entity user local thành đối tượng phiên rút gọn để dùng trong service hoặc controller.
     *
     * @param user user local đã được xác thực thành công
     * @param provider nguồn đăng nhập tương ứng của phiên hiện tại
     * @return {@link UserSession} đã chuẩn hóa từ dữ liệu local của người dùng
     */
    private UserSession buildUserSession(User user, AuthProvider provider) {
        // Step 1: lấy các trường cốt lõi từ user local đang nằm trong principal.
        // Step 2: đóng gói các trường này thành UserSession để các tầng còn lại dùng thống nhất.
        return new UserSession(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                provider.name(),
                true
        );
    }

    /**
     * Xác định provider đăng nhập của principal OAuth2 hiện tại.
     *
     * @param oauth2User principal OAuth2 hoặc OIDC đã được map sang user local
     * @return {@link AuthProvider} tương ứng với nguồn đăng nhập của principal
     */
    private AuthProvider resolveProvider(CustomOAuth2User oauth2User) {
        // Step 1: đọc attribute `sub` để nhận diện Google OIDC nếu principal là OIDC user.
        if (oauth2User instanceof CustomOidcUser) {
            return AuthProvider.GOOGLE;
        }

        // Step 2: suy luận provider từ bộ attribute phổ biến của provider hiện tại.
        if (oauth2User.getAttributes().containsKey("sub")) {
            return AuthProvider.GOOGLE;
        }
        if (oauth2User.getAttributes().containsKey("id")) {
            return AuthProvider.FACEBOOK;
        }
        return AuthProvider.LOCAL;
    }
}
