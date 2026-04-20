package vn.edu.bkis.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.edu.bkis.model.User;

public class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;

    // Gói thông tin user local cùng dữ liệu OAuth2 để dùng chung trong toàn bộ ứng dụng.
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // Trả về quyền dựa trên role local của hệ thống thay vì phụ thuộc trực tiếp vào provider.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // Trả về toàn bộ attributes mà provider gửi về cho phiên đăng nhập hiện tại.
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // Trả về tên đại diện cho principal để Spring Security sử dụng trong session hiện tại.
    @Override
    public String getName() {
        return user.getUsername();
    }

    // Trả về user local đã được link hoặc tạo từ tài khoản SSO.
    public User getUser() {
        return user;
    }
}
