package vn.edu.bkis.service;

import java.security.SecureRandom;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    private final SecureRandom random = new SecureRandom();

    public String generateQuestion(HttpSession session) {
        int a = 1 + random.nextInt(9);
        int b = 1 + random.nextInt(9);
        int result = a + b;
        session.setAttribute("captchaExpected", String.valueOf(result));
        return a + " + " + b + " = ?";
    }
}
