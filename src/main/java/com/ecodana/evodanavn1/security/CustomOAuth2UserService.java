package com.ecodana.evodanavn1.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.service.UserService;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Get the default OidcUser from Google
        OidcUser oidcUser = super.loadUser(userRequest);
        
        // Extract email from Google response
        String email = oidcUser.getAttribute("email");
        System.out.println("CustomOAuth2UserService - Processing OIDC user with email: " + email);
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("No email provided by OAuth2 provider");
        }
        
        // Find user in our database
        User user = userService.findByEmailWithRole(email);
        if (user == null) {
            System.out.println("CustomOAuth2UserService - User not found in database, will be created with default role");
            // User will be created later in OAuth2LoginSuccessHandler
            return oidcUser;
        }
        
        System.out.println("CustomOAuth2UserService - Found user in database: " + user.getEmail() + " with role: " + user.getRoleName());
        
        // Create authorities based on user's role
        Collection<? extends GrantedAuthority> authorities = createAuthorities(user);
        
        // Create custom OidcUser with our user's authorities
        return new CustomOidcUser(oidcUser.getClaims(), oidcUser.getIdToken(), oidcUser.getUserInfo(), authorities, user);
    }
    
    private Collection<? extends GrantedAuthority> createAuthorities(User user) {
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            String roleName = user.getRole().getRoleName().toUpperCase();
            System.out.println("CustomOAuth2UserService - Creating authority for role: " + roleName);
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
        }
        System.out.println("CustomOAuth2UserService - No role found, using default authority");
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
    
    // Custom OidcUser implementation that includes our User object
    public static class CustomOidcUser implements OidcUser {
        private final Map<String, Object> claims;
        private final OidcIdToken idToken;
        private final OidcUserInfo userInfo;
        private final Collection<? extends GrantedAuthority> authorities;
        private final User user;
        
        public CustomOidcUser(Map<String, Object> claims, 
                             OidcIdToken idToken,
                             OidcUserInfo userInfo,
                             Collection<? extends GrantedAuthority> authorities, 
                             User user) {
            this.claims = claims;
            this.idToken = idToken;
            this.userInfo = userInfo;
            this.authorities = authorities;
            this.user = user;
        }
        
        @Override
        public Map<String, Object> getClaims() {
            return claims;
        }
        
        @Override
        public OidcIdToken getIdToken() {
            return idToken;
        }
        
        @Override
        public OidcUserInfo getUserInfo() {
            return userInfo;
        }
        
        @Override
        public Map<String, Object> getAttributes() {
            return claims;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public String getName() {
            return user.getUsername();
        }
        
        public User getUser() {
            return user;
        }
    }
}
