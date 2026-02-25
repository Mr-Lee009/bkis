package vn.edu.bkis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vn.edu.bkis.service.CaptchaService;

@Controller
public class AuthController {
    private final CaptchaService captchaService;

    public AuthController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model,
                            HttpSession session) {
//        String question;
//        Object q = session.getAttribute("captchaQuestion");
//        if (error == null && q == null) {
//            question = captchaService.generateQuestion(session);
//        } else {
//            question = (q == null ? captchaService.generateQuestion(session) : q.toString());
//        }
//        model.addAttribute("captchaQuestion", question);
        model.addAttribute("error", error);
        return "01-login";
    }
}
