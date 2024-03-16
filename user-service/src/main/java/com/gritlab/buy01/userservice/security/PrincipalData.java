package com.gritlab.buy01.userservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class PrincipalData {
  private final boolean isAdmin;
  private final UserDetailsImpl userDetails;

  public PrincipalData() {
    this.userDetails =
        (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    this.isAdmin =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);
  }

  public boolean isAdmin() {
    return this.isAdmin;
  }

  public boolean isSelf(String id) {
    return this.userDetails.getId().equals(id);
  }

  public UserDetailsImpl getUserDetails() {
    return this.userDetails;
  }

  public boolean authCheck(String id) {
    return this.isAdmin || this.isSelf(id);
  }
}
