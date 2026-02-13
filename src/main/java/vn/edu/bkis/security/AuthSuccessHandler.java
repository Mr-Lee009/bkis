package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public AuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            var user = cud.getUser();
            user.setFailedLoginAttempts(0);
            user.setLocked(false);
            userRepository.save(user);
        }
        response.sendRedirect("/");
    }
}
