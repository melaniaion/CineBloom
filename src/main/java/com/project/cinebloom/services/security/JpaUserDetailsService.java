package com.project.cinebloom.services.security;

import com.project.cinebloom.domain.Authority;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

import com.project.cinebloom.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@RequiredArgsConstructor
@Slf4j
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;

        Optional<User> userOpt= userRepository.findByUsername(username);
        if (userOpt.isPresent())
            user = userOpt.get();
        else
            throw new UsernameNotFoundException("Username: " + username);

        log.info(user.toString());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getAccountNonLocked(),
                getAuthorities(user.getAuthorities()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Authority> authorities) {
        if (authorities == null){
            return new HashSet<>();
        } else if (authorities.size() == 0){
            return new HashSet<>();
        }
        else{
            return authorities.stream()
                    .map(Authority::getRole)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
    }
}
