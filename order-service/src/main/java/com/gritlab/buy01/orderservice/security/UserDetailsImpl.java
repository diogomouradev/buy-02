package com.gritlab.buy01.orderservice.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.gritlab.buy01.orderservice.exception.UnexpectedPrincipalTypeException;
import com.gritlab.buy01.orderservice.model.User;

public class UserDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 1L;

  private String id;

  private String name;

  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(
      String id, String name, Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.name = name;
    this.authorities = authorities;
  }

  public static UserDetailsImpl build(User user) {
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

    return new UserDetailsImpl(user.getId(), user.getName(), authorities);
  }

  public boolean hasRole(String role) {
    return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  public String getId() {
    return id;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return name;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserDetailsImpl user = (UserDetailsImpl) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((authorities == null) ? 0 : authorities.hashCode());
    return result;
  }

  public static UserDetailsImpl getPrincipal() throws UnexpectedPrincipalTypeException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetailsImpl userDetailsImpl) {
      return userDetailsImpl;
    } else {
      throw new UnexpectedPrincipalTypeException("Unexpected principal type");
    }
  }
}
