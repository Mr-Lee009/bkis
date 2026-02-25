package vn.edu.bkis.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {
    private final UserRepository userRepository;

    public AuthFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Skip redirect if captcha failed
//        if (request.getAttribute("captchaFailed") != null) {
//            System.out.println("[AuthFailureHandler] Captcha failed, skipping redirect.");
//            return;
//        }
        String username = request.getParameter("username");
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
            attempts++;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 10) {
                user.setLocked(true);
            }
            userRepository.save(user);
        });
        request.setAttribute("captchaFailed", null);
        response.sendRedirect("/login?error");
    }
}
