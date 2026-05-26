package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import vn.edu.bkis.constan.ConstantCommon;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * Khởi tạo handler xử lý sau khi người dùng đăng nhập thành công.
     *
     * @param userRepository repository dùng để cập nhật trạng thái đăng nhập sai của user local
     * @return không trả dữ liệu; constructor dùng để gán dependency cho handler
     */
    public AuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Xử lý redirect sau đăng nhập thành công và ưu tiên quay lại URL mà người dùng đang truy cập dở.
     *
     * @param request request hiện tại chứa session và saved request trước login
     * @param response response dùng để redirect người dùng sau khi xác thực xong
     * @param authentication thông tin xác thực thành công của người dùng hiện tại
     * @return không trả dữ liệu; method ghi redirect trực tiếp vào response
     * @throws IOException ném ra khi response không thể ghi redirect
     * @throws ServletException ném ra khi container servlet báo lỗi trong luồng xác thực
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Step 1: đồng bộ lại trạng thái tài khoản local sau khi người dùng xác thực thành công.
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            resetLoginAttempts(cud.getUser());
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            resetLoginAttempts(customOAuth2User.getUser());
        }

        // Step 2: đọc URL mà Spring Security đã lưu trước khi đẩy người dùng sang trang login.
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }

        // Step 3: nếu không có URL trước đó thì fallback về trang chủ như luồng mặc định.
        response.sendRedirect("/");
    }

    /**
     * Đặt lại bộ đếm đăng nhập sai và mở khóa tài khoản local sau khi xác thực thành công.
     *
     * @param user thực thể user local cần cập nhật trạng thái đăng nhập
     * @return không trả dữ liệu; method lưu trực tiếp trạng thái mới của user xuống database
     */
    private void resetLoginAttempts(vn.edu.bkis.model.User user) {
        // Step 1: đưa bộ đếm đăng nhập sai về 0 và mở khóa nếu tài khoản từng bị khóa tạm thời.
        user.setFailedLoginAttempts(ConstantCommon.ZERO_NUMBER);
        user.setLocked(false);

        // Step 2: lưu trạng thái tài khoản mới để các lần đăng nhập sau không bị ảnh hưởng.
        userRepository.save(user);
    }
}
