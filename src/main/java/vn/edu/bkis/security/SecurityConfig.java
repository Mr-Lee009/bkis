package vn.edu.bkis.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    private final CaptchaFilter captchaFilter;
    private final AuthFailureHandler authFailureHandler;
    private final AuthSuccessHandler authSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final String rememberMeKey;
    private final int rememberMeValiditySeconds;

    // Khởi tạo cấu hình bảo mật và nạp các tham số cần cho tính năng ghi nhớ đăng nhập.
    public SecurityConfig(
//            CaptchaFilter captchaFilter,
            AuthFailureHandler authFailureHandler,
            AuthSuccessHandler authSuccessHandler,
            CustomUserDetailsService customUserDetailsService,
            @Value("${app.security.remember-me.key}") String rememberMeKey,
            @Value("${app.security.remember-me.validity-seconds}") int rememberMeValiditySeconds) {
//        this.captchaFilter = captchaFilter;
        this.authFailureHandler = authFailureHandler;
        this.authSuccessHandler = authSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.rememberMeKey = rememberMeKey;
        this.rememberMeValiditySeconds = rememberMeValiditySeconds;
    }

    // Cấu hình chuỗi filter bảo mật và bật cơ chế remember-me bằng cookie token.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/captcha", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // Đổi sang /do-login
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler)
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .key(rememberMeKey)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("BKIS_REMEMBER_ME")
                        .tokenValiditySeconds(rememberMeValiditySeconds)
                        .userDetailsService(customUserDetailsService))
                .logout(logout -> logout.logoutUrl("/logout").permitAll());

//        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Tạo bộ mã hóa mật khẩu dùng chung cho toàn bộ luồng xác thực.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cung cấp AuthenticationManager để các luồng đăng nhập sử dụng xác thực chuẩn của Spring Security.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
