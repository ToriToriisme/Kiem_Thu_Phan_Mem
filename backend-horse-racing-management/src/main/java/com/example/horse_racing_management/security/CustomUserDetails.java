package com.example.horse_racing_management.security;

import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private String username;
    private String password;
    private Boolean status;
    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole().getKey()));
            
            if (user.getRole().getPermissions() != null) {
                for (Permission p : user.getRole().getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(p.getKey()));
                }
            }
        }

        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() != null ? user.getStatus() : true,
                authorities
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status;
    }
}