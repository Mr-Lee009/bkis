package vn.edu.bkis.security;

import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import vn.edu.bkis.model.User;

public class CustomOidcUser extends CustomOAuth2User implements OidcUser {

    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    // Gói user local cùng dữ liệu OIDC để Google SSO vẫn dùng được principal.user trên UI.
    public CustomOidcUser(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        super(user, attributes);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    // Trả về claims chuẩn OIDC để Spring Security vẫn xử lý đúng phiên đăng nhập Google.
    @Override
    public Map<String, Object> getClaims() {
        return getAttributes();
    }

    // Trả về ID token do Google cấp cho phiên đăng nhập hiện tại.
    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    // Trả về user info OIDC nếu provider có cung cấp.
    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }
}
