package se.iths.stefan._faauthenticator.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.iths.stefan._faauthenticator.model.AppUser;
import se.iths.stefan._faauthenticator.repository.AppUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;

    public AppUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String authority = (appUser.getRole() == null || appUser.getRole().isBlank())
                ? "ROLE_USER"
                : appUser.getRole();

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPassword()) // already BCrypt encoded
                .authorities(authority)
                .build();
    }
}
