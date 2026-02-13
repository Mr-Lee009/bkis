package vn.edu.bkis.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CaptchaFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("/login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String answer = request.getParameter("captchaAnswer");
            var session = request.getSession(false);
            String expected = null;
            if (session != null) {
                Object expectedObj = session.getAttribute("captchaExpected");
                expected = expectedObj == null ? null : expectedObj.toString();
            }

            if (!matchesCaptcha(answer, expected)) {
                response.sendRedirect("/login?error=captcha");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean matchesCaptcha(String answer, String expected) {
        if (answer == null || expected == null) return false;
        try {
            int a = Integer.parseInt(answer.trim());
            int e = Integer.parseInt(expected.trim());
            return a == e;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
