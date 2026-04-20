package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    // Khởi tạo handler xử lý sau khi người dùng đăng nhập thành công.
    public AuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Đồng bộ trạng thái tài khoản local sau khi đăng nhập thành công rồi chuyển người dùng về trang chủ.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            resetLoginAttempts(cud.getUser());
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            resetLoginAttempts(customOAuth2User.getUser());
        }
        response.sendRedirect("/");
    }

    // Đặt lại bộ đếm đăng nhập sai cho user local sau khi xác thực thành công.
    private void resetLoginAttempts(vn.edu.bkis.model.User user) {
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        userRepository.save(user);
    }
}
