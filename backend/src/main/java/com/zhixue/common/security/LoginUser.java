package com.zhixue.common.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private Integer tokenVersion;
    private Integer status;            // 0正常 1停用
    private String roleKey;            // 角色标识 admin/teacher/student
    private Set<String> permissions;   // 权限标识集合

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        // 将角色标识也注入权限，使 @PreAuthorize("hasAuthority('admin')") 对超级管理员生效
        if (roleKey != null && !roleKey.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(roleKey));
        }
        return authorities;
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status == null || status == 0; }
}
