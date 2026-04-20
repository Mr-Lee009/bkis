package vn.edu.bkis.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    private final CaptchaFilter captchaFilter;
    private final AuthFailureHandler authFailureHandler;
    private final AuthSuccessHandler authSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String rememberMeKey;
    private final int rememberMeValiditySeconds;

    // Khởi tạo cấu hình bảo mật và nạp các tham số cần cho tính năng ghi nhớ đăng nhập.
    public SecurityConfig(
//            CaptchaFilter captchaFilter,
            AuthFailureHandler authFailureHandler,
            AuthSuccessHandler authSuccessHandler,
            CustomUserDetailsService customUserDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            @Value("${app.security.remember-me.key}") String rememberMeKey,
            @Value("${app.security.remember-me.validity-seconds}") int rememberMeValiditySeconds) {
//        this.captchaFilter = captchaFilter;
        this.authFailureHandler = authFailureHandler;
        this.authSuccessHandler = authSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        this.rememberMeKey = rememberMeKey;
        this.rememberMeValiditySeconds = rememberMeValiditySeconds;
    }

    // Cấu hình chuỗi filter bảo mật và bật cơ chế remember-me bằng cookie token.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/captcha", "/css/**", "/js/**", "/img/**",
                                "/favicon.ico", "/oauth2/**", "/login/oauth2/**").permitAll()
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

        // Chỉ bật OAuth2 login khi ứng dụng đã có ít nhất một cấu hình client registration hợp lệ.
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                    .successHandler(authSuccessHandler)
                    .permitAll());
        }

//        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
