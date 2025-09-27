package com.ecodana.evodanavn1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

import com.ecodana.evodanavn1.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    private final CustomAuthenticationSuccessHandler customSuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler,
                         CustomAuthenticationSuccessHandler customSuccessHandler,
                         ClientRegistrationRepository clientRegistrationRepository,
                         UserService userService,
                         PasswordEncoder passwordEncoder) {
        this.successHandler = successHandler;
        this.customSuccessHandler = customSuccessHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.ecodana.evodanavn1.model.User user = userService.findByUsername(username);
            if (user == null) {
                user = userService.findByEmail(username);
            }
            if (user == null) {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username);
            }
            return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoleName().toUpperCase())
                .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService());
        return new ProviderManager(authProvider);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationManager(authenticationManager())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/login-success", "/logout", "/css/**", "/js/**", "/images/**", "/oauth2/**", "/test/**", "/admin-simple", "/admin-test").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/owner/**").hasAnyRole("ADMIN", "STAFF", "OWNER")
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/oauth2/**", "/api/**", "/admin/api/**", "/test/**")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(successHandler)
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                )
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry())
            );
        return http.build();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver authorizationRequestResolver =
            new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        authorizationRequestResolver.setAuthorizationRequestCustomizer(
            customizer -> customizer.additionalParameters(params -> {
                params.put("access_type", "offline");
                params.put("prompt", "consent select_account");
            })
        );
        return authorizationRequestResolver;
    }

}
