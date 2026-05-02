package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.bkis.common.BusinessException;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.common.MessageResolver;
import vn.edu.bkis.dto.RegisterAccountFormDto;
import vn.edu.bkis.service.UserRegistrationService;

@Controller
public class RegisterController {
    private final UserRegistrationService userRegistrationService;
    private final MessageResolver messageResolver;

    // Khoi tao controller xu ly man hinh dang ky tai khoan nguoi dung.
    public RegisterController(UserRegistrationService userRegistrationService, MessageResolver messageResolver) {
        this.userRegistrationService = userRegistrationService;
        this.messageResolver = messageResolver;
    }

    // Hien thi form dang ky tai khoan hoc vien moi.
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", messageResolver.get("page.register.title"));
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterAccountFormDto());
        }
        return "10-register";
    }

    // Tao tai khoan moi va chuyen nguoi dung ve man hinh dang nhap khi thanh cong.
    @PostMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterAccountFormDto form,
                           RedirectAttributes redirectAttributes) {
        try {
            userRegistrationService.register(form);
            redirectAttributes.addFlashAttribute("successMessage", messageResolver.get(MessageCode.S0001));
            return "redirect:/login";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", messageResolver.get(ex.getCode(), ex.getArgs()));
            redirectAttributes.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
    }
}
