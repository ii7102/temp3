package com.pulsefit.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    Object realmAccess = jwt.getClaim("realm_access");
    if (realmAccess instanceof Map<?, ?> accessMap) {
      Object roles = accessMap.get("roles");
      if (roles instanceof Collection<?> collection) {
        for (Object role : collection) {
          authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
      }
    }
    return authorities;
  }
}
