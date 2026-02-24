package vn.edu.bkis.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class CaptchaService {
    private final SecureRandom random = new SecureRandom();

    public String generateQuestion(HttpSession session) {
        int a = 1 + random.nextInt(9);
        int b = 1 + random.nextInt(9);
        int result = a + b;
        String question = a + " + " + b + " = ?";
        session.setAttribute("captchaExpected", String.valueOf(result));
        session.setAttribute("captchaQuestion", question);
        return question;
    }
}
