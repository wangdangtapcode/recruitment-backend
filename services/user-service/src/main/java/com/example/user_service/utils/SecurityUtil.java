package com.example.user_service.utils;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(auth -> auth.getCredentials() instanceof String)
                .map(auth -> (String) auth.getCredentials());
    }

    // public static boolean isAuthenticated(){
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // return authentication!= null &&
    // getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANOMYMOUS::equal)
    // ;
    // }

    // public static boolean hasCurrentUserAnyOfAuthorities(String... authorities){
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // return authentication!= null && ;
    // }

    // public static boolean hasCurrentUserNoneOfAuthorities(String... authorities)
    // {
    // return !hasCurrentUserAnyOfAuthorities(authorities);
    // }

    // public static boolean hasCurrentUserThisAuthority(String authority) {
    // return hasCurrentUserAnyOfAuthorities(authority);
    // }

    // public static Steam<String> getAuthorities(Authentication authentication) {
    // return
    // authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    // }
}
