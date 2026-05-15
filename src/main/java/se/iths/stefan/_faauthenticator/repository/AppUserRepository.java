package se.iths.stefan._faauthenticator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iths.stefan._faauthenticator.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    
}
