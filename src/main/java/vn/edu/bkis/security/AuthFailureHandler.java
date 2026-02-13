package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
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
        response.sendRedirect("/login?error");
    }
}
