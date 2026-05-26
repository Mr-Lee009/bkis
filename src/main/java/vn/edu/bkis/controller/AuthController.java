package vn.edu.bkis.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.common.MessageResolver;

import jakarta.servlet.http.HttpSession;
import vn.edu.bkis.service.CaptchaService;

@Controller
public class AuthController {

    // Luu cau hinh hien thi SSO va service captcha cho man hinh dang nhap.
    private final boolean googleSsoEnabled;
    private final boolean facebookSsoEnabled;
    private final CaptchaService captchaService;
    private final MessageResolver messageResolver;

    // Khoi tao cau hinh hien thi cac nut dang nhap SSO tren man hinh login.
    public AuthController(
        @Value("${app.security.sso.google-enabled:false}") boolean googleSsoEnabled,
        @Value("${app.security.sso.facebook-enabled:false}") boolean facebookSsoEnabled,
        MessageResolver messageResolver, CaptchaService captchaService) {
        this.googleSsoEnabled = googleSsoEnabled;
        this.facebookSsoEnabled = facebookSsoEnabled;
        this.messageResolver = messageResolver;
        this.captchaService = captchaService;
    }

    // Hien thi man hinh dang nhap va dua thong bao loi hien tai ra giao dien.
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

    // Chuan hoa thong bao loi dang nhap va captcha truoc khi dua ra giao dien.
    private String resolveLoginErrorMessage(String error, String errorCode, String errorArg) {
        if (errorCode != null && !errorCode.isBlank()) {
            return errorArg == null || errorArg.isBlank()
                    ? messageResolver.get(errorCode)
                    : messageResolver.get(errorCode, errorArg);
        }
        if (error == null) {
            return null;
        }
        if ("captcha".equalsIgnoreCase(error)) {
            return "Captcha khong dung hoac da het han. Vui long thu lai.";
        }
        if (error.isBlank() || "true".equalsIgnoreCase(error)) {
            return messageResolver.get(MessageCode.E0023);
        }
        return error;
    }

    // Tra ve anh captcha moi va luu dap an vao session cho luong login thuong.
    @GetMapping("/captcha/image")
    public ResponseEntity<byte[]> captchaImage(HttpSession session) {
        byte[] imageBytes = captchaService.generateCaptchaImage(session);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
            .body(imageBytes);
    }
}
