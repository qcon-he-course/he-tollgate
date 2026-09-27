package com.halvern.tollgate.platform;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The signed-in staff user.
 *
 * @author Dana Kerr
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static String name() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (("ROLE_" + role).equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
