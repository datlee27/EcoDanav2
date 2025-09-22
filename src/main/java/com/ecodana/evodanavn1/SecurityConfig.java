package com.ecodana.evodanavn1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import com.ecodana.evodanavn1.security.OAuth2LoginSuccessHandler;

@Configuration
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler, ClientRegistrationRepository clientRegistrationRepository) {
        this.successHandler = successHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**", "/oauth2/**").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(successHandler)
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                )
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
