package vn.edu.bkis.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.bkis.service.CaptchaService;

@Component
public class CaptchaFilter extends OncePerRequestFilter {
    // Service responsible for validating the submitted captcha against the session value.
    private final CaptchaService captchaService;

    /**
     * @param captchaService service used to validate captcha before authentication continues
     */
    public CaptchaFilter(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    /**
     * Intercepts login POST requests and rejects them early when captcha validation fails.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain remaining filters to execute when captcha is valid
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("/login".equals(request.getServletPath()) && "POST".equalsIgnoreCase(request.getMethod())) {
            // Existing session containing the captcha answer created when the captcha image was loaded.
            HttpSession session = request.getSession(false);

            // Raw captcha text submitted from the login form.
            String answer = request.getParameter("captchaAnswer");
            if (session == null || !captchaService.matches(answer, session)) {
                response.sendRedirect("/login?error=captcha");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
