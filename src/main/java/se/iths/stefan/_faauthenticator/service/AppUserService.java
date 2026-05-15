package se.iths.stefan._faauthenticator.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.iths.stefan._faauthenticator.model.AppUser;
import se.iths.stefan._faauthenticator.repository.AppUserRepository;

@Service
public class AppUserService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    public AppUser createUser(AppUser appUser) {
        String encryptedPassword = passwordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encryptedPassword);

        if (appUser.getRole() == null || appUser.getRole().isBlank()) {
            appUser.setRole("USER");
        }

        return repository.save(appUser);
    }

    public AppUser enableTwoFactor(Long userId) {
        AppUser appUser = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        appUser.setTwoFactorEnabled(true);
        return repository.save(appUser);
    }
    

    public AppUser setupTwoFactor(Long userId) {
        AppUser appUser = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Sätt 2FA som enabled
        appUser.setTwoFactorEnabled(true);
        
        // Generera hemlig nyckel om den inte finns
        if (appUser.getTwoFactorSecret() == null || appUser.getTwoFactorSecret().isBlank()) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey credentials = gAuth.createCredentials();
            appUser.setTwoFactorSecret(credentials.getKey());
        }
        
        // Spara till databasen
        return repository.save(appUser);
    }

    public AppUser prepareTwoFactor(Long userId) {
        AppUser appUser = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (appUser.isTwoFactorEnabled() && appUser.getTwoFactorSecret() == null) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey credentials = gAuth.createCredentials();
            appUser.setTwoFactorSecret(credentials.getKey());
            repository.save(appUser);
        }

        return appUser;
    }

    public boolean verifyTwoFactorCode(Long userId, int code) {
        AppUser appUser = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!appUser.isTwoFactorEnabled()) {
            return false;
        }

        String secret = appUser.getTwoFactorSecret();
        if (secret == null || secret.isBlank()) {
            return false;
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.authorize(secret, code);
    }
}
