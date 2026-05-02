package vn.edu.bkis.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.common.MessageResolver;

@Controller
public class AuthController {

    private final boolean googleSsoEnabled;
    private final boolean facebookSsoEnabled;
    private final MessageResolver messageResolver;

    // Khoi tao cau hinh hien thi cac nut dang nhap SSO tren man hinh login.
    public AuthController(@Value("${app.security.sso.google-enabled:false}") boolean googleSsoEnabled,
                          @Value("${app.security.sso.facebook-enabled:false}") boolean facebookSsoEnabled,
                          MessageResolver messageResolver) {
        this.googleSsoEnabled = googleSsoEnabled;
        this.facebookSsoEnabled = facebookSsoEnabled;
        this.messageResolver = messageResolver;
    }

    // Hien thi man hinh dang nhap va resolve loi dang nhap theo ngon ngu hien tai.
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "errorCode", required = false) String errorCode,
                            @RequestParam(value = "errorArg", required = false) String errorArg,
                            Model model) {
        model.addAttribute("errorMessage", resolveLoginErrorMessage(error, errorCode, errorArg));
        model.addAttribute("googleSsoEnabled", googleSsoEnabled);
        model.addAttribute("facebookSsoEnabled", facebookSsoEnabled);
        return "01-login";
    }

    // Chuan hoa thong bao loi dang nhap truoc khi dua ra giao dien.
    private String resolveLoginErrorMessage(String error, String errorCode, String errorArg) {
        if (errorCode != null && !errorCode.isBlank()) {
            return errorArg == null || errorArg.isBlank()
                    ? messageResolver.get(errorCode)
                    : messageResolver.get(errorCode, errorArg);
        }
        if (error == null) {
            return null;
        }
        if (error.isBlank() || "true".equalsIgnoreCase(error)) {
            return messageResolver.get(MessageCode.E0023);
        }
        return error;
    }
}
