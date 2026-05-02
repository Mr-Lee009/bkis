package vn.edu.bkis.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import vn.edu.bkis.common.MessageCode;
import vn.edu.bkis.repository.UserRepository;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {
    private final UserRepository userRepository;

    // Khoi tao handler xu ly khi dang nhap that bai.
    public AuthFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Chuyen loi xac thuc thanh ma message i18n va redirect ve man hinh login.
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        LoginFailureMessage failureMessage = resolveFailureMessage(exception);
        if (username != null && !username.isBlank()) {
            failureMessage = userRepository.findByUsername(username)
                    .map(user -> {
                        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
                        attempts++;
                        user.setFailedLoginAttempts(attempts);
                        if (Boolean.TRUE.equals(user.getLocked()) || attempts >= 10) {
                            user.setLocked(true);
                            userRepository.save(user);
                            return new LoginFailureMessage(MessageCode.E0025);
                        }
                        userRepository.save(user);
                        return new LoginFailureMessage(MessageCode.E0027, String.valueOf(10 - attempts));
                    })
                    .orElse(new LoginFailureMessage(MessageCode.E0023));
        }
        request.setAttribute("captchaFailed", null);
        response.sendRedirect(buildLoginErrorUrl(failureMessage));
    }

    // Chuan hoa loi xac thuc cua Spring Security thanh ma message cua he thong.
    private LoginFailureMessage resolveFailureMessage(AuthenticationException exception) {
        if (exception instanceof LockedException) {
            return new LoginFailureMessage(MessageCode.E0025);
        }
        if (exception instanceof UsernameNotFoundException) {
            return new LoginFailureMessage(MessageCode.E0023);
        }
        if (exception instanceof OAuth2AuthenticationException) {
            String detail = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? ""
                    : exception.getMessage();
            return detail.isBlank()
                    ? new LoginFailureMessage(MessageCode.E0029)
                    : new LoginFailureMessage(MessageCode.E0029, detail);
        }
        return new LoginFailureMessage(MessageCode.E0023);
    }

    // Tao URL redirect gom message code va tham so dong neu co.
    private String buildLoginErrorUrl(LoginFailureMessage failureMessage) {
        String url = "/login?errorCode=" + encode(failureMessage.code());
        if (failureMessage.arg() != null) {
            url += "&errorArg=" + encode(failureMessage.arg());
        }
        return url;
    }

    // Encode gia tri query string truoc khi redirect.
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record LoginFailureMessage(String code, String arg) {
        private LoginFailureMessage(String code) {
            this(code, null);
        }
    }
}
