package vn.edu.bkis.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class UserSessionConfiguration {

    /**
     * Tạo bean UserSession theo từng request để controller và service có thể inject trực tiếp.
     *
     * @param userSessionProvider provider đọc principal hiện tại từ SecurityContext
     * @return {@link UserSession} của request hiện tại hoặc phiên ẩn danh nếu chưa đăng nhập
     */
    @Bean
    @RequestScope
    public UserSession userSession(UserSessionProvider userSessionProvider) {
        // Step 1: đọc principal hiện tại từ SecurityContext thông qua provider trung gian.
        // Step 2: trả về UserSession của người dùng hiện tại hoặc phiên anonymous để tránh null ở bean scope request.
        return userSessionProvider.getCurrentUserSessionOptional().orElse(UserSession.anonymous());
    }
}
