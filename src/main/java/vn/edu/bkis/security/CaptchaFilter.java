package vn.edu.bkis.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                System.out.println("[CaptchaFilter] Captcha failed: answer='" + answer + "', expected='" + expected + "'. Redirecting to /login?error=captcha and stopping filter chain.");
                request.setAttribute("captchaFailed", true);
                response.sendRedirect("/login?error=captcha");
                return;
            } else {
                System.out.println("[CaptchaFilter] Captcha passed: answer='" + answer + "', expected='" + expected + "'.");
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
