package vn.edu.bkis.controller;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.common.BusinessException;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.common.MessageResolver;
import vn.edu.bkis.dto.ForgotPasswordFormDto;
import vn.edu.bkis.dto.ResetPasswordFormDto;
import vn.edu.bkis.service.PasswordResetService;

@Controller
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService passwordResetService;
    private final MessageResolver messageResolver;

    // Khoi tao controller xu ly cac man hinh quen mat khau.
    public PasswordResetController(PasswordResetService passwordResetService, MessageResolver messageResolver) {
        this.passwordResetService = passwordResetService;
        this.messageResolver = messageResolver;
    }

    // Hien thi form nhap email hoac username de yeu cau dat lai mat khau.
    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("pageTitle", messageResolver.get("page.forgotPassword.title"));
        if (!model.containsAttribute("forgotPasswordForm")) {
            model.addAttribute("forgotPasswordForm", new ForgotPasswordFormDto());
        }
        return "08-forgot-password";
    }

    // Tao token reset password va ghi link reset ra log trong moi truong dev.
    @PostMapping("/forgot-password")
    public String requestReset(@ModelAttribute("forgotPasswordForm") ForgotPasswordFormDto form,
                               RedirectAttributes redirectAttributes) {
        Optional<String> token = passwordResetService.requestReset(form.getAccount());
        redirectAttributes.addFlashAttribute("successMessage", messageResolver.get(MessageCode.S0003));
        token.ifPresent(value -> log.info("Password reset dev link: /reset-password?token={}", value));
        return "redirect:/forgot-password";
    }

    // Hien thi form dat lai mat khau khi token hop le.
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, Model model) {
        model.addAttribute("pageTitle", messageResolver.get("page.resetPassword.title"));
        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("errorMessage", messageResolver.get(MessageCode.E0019));
            return "09-reset-password";
        }
        ResetPasswordFormDto form = new ResetPasswordFormDto();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        return "09-reset-password";
    }

    // Cap nhat mat khau moi neu token hop le roi chuyen nguoi dung ve man hinh login.
    @PostMapping("/reset-password")
    public String resetPassword(@ModelAttribute("resetPasswordForm") ResetPasswordFormDto form,
                                RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(form.getToken(), form.getPassword(), form.getConfirmPassword());
            redirectAttributes.addFlashAttribute("successMessage", messageResolver.get(MessageCode.S0005));
            return "redirect:/login";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messageResolver.get(ex.getCode(), ex.getArgs()));
            return "redirect:/reset-password?token=" + form.getToken();
        }
    }
}
