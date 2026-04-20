package vn.edu.bkis.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final boolean googleSsoEnabled;
    private final boolean facebookSsoEnabled;

    // Khởi tạo cấu hình hiển thị các nút đăng nhập SSO trên màn hình login.
    public AuthController(@Value("${app.security.sso.google-enabled:false}") boolean googleSsoEnabled,
                          @Value("${app.security.sso.facebook-enabled:false}") boolean facebookSsoEnabled) {
        this.googleSsoEnabled = googleSsoEnabled;
        this.facebookSsoEnabled = facebookSsoEnabled;
    }

    // Hiển thị màn hình đăng nhập và truyền trạng thái các nhà cung cấp SSO cho giao diện.
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        model.addAttribute("error", error);
        model.addAttribute("googleSsoEnabled", googleSsoEnabled);
        model.addAttribute("facebookSsoEnabled", facebookSsoEnabled);
        return "01-login";
    }
}
