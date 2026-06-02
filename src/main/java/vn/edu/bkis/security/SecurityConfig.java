package vn.edu.bkis.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Filter captcha duoc chen truoc filter login thuong de chan request sai som.
    private final CaptchaFilter captchaFilter;
    private final AuthFailureHandler authFailureHandler;
    private final AuthSuccessHandler authSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String rememberMeKey;
    private final int rememberMeValiditySeconds;

    // Khoi tao cau hinh bao mat va nap cac thanh phan xac thuc can thiet.
    public SecurityConfig(
            CaptchaFilter captchaFilter,
            AuthFailureHandler authFailureHandler,
            AuthSuccessHandler authSuccessHandler,
            CustomUserDetailsService customUserDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            @Value("${app.security.remember-me.key}") String rememberMeKey,
            @Value("${app.security.remember-me.validity-seconds}") int rememberMeValiditySeconds) {
        this.captchaFilter = captchaFilter;
        this.authFailureHandler = authFailureHandler;
        this.authSuccessHandler = authSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        this.rememberMeKey = rememberMeKey;
        this.rememberMeValiditySeconds = rememberMeValiditySeconds;
    }

    // Cau hinh chuoi filter bao mat cho login thuong, SSO va cac vung du lieu theo role.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Mo public cho login, captcha server-side, static asset, file preview va callback OAuth2.
                        .requestMatchers("/login", "/register", "/forgot-password", "/reset-password", "/captcha/**",
                                "/access-denied", "/css/**", "/js/**", "/img/**", "/uploads/**", "/favicon.ico", "/oauth2/**",
                                "/login/oauth2/**").permitAll()
                        // Chi hoc vien da dang nhap moi duoc vao cac luong hoc tap ca nhan.
                        .requestMatchers("/my-courses").hasRole("STUDENT")
                        .requestMatchers("/courses/*/signup").hasRole("STUDENT")
                        // Trang cong khai cho phep xem thong tin khoa hoc truoc khi mua hoc.
                        .requestMatchers("/", "/courses/**").permitAll()
                        // Cac khu vuc admin nhay cam chi cho ADMIN truy cap.
                        .requestMatchers("/admin/accounts/**", "/admin/dashboard/**", "/admin/payment-gateways/**",
                                "/api/admin/payment-gateways/**").hasRole("ADMIN")
                        // Quan ly hoc vien cho phep ADMIN va TEACHER.
                        .requestMatchers("/admin/students/**", "/api/admin/students/**")
                        .hasAnyRole("ADMIN", "TEACHER")
                        // Quan ly noi dung khoa hoc va upload mo cho nhom giang day.
                        .requestMatchers("/admin/courses/**", "/upload/**")
                        .hasAnyRole("ADMIN", "TEACHER", "INSTRUCTOR")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        // Giu login processing tai /login de CaptchaFilter chi can chan mot diem vao.
                        .loginProcessingUrl("/login")
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler)
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .key(rememberMeKey)
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("BKIS_REMEMBER_ME")
                        .tokenValiditySeconds(rememberMeValiditySeconds)
                        .userDetailsService(customUserDetailsService))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // Nguoi dung da dang nhap nhung khong du quyen se duoc redirect sang man hinh 403 chung.
                        .accessDeniedPage("/access-denied"))
                .logout(logout -> logout.logoutUrl("/logout").permitAll());

        // Captcha chi ap cho form login thuong vi filter nay chan truoc UsernamePasswordAuthenticationFilter.
        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);

        // Chi bat OAuth2 login khi ung dung da co it nhat mot client registration hop le.
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    // Tai thong tin tu provider va map ve user local theo role cua he thong.
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOAuth2UserService::loadOidcUser))
                    .failureHandler(authFailureHandler)
                    .successHandler(authSuccessHandler)
                    .permitAll());
        }

        return http.build();
    }
}
