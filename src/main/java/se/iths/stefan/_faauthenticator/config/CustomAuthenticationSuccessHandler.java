package se.iths.stefan._faauthenticator.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import se.iths.stefan._faauthenticator.model.AppUser;
import se.iths.stefan._faauthenticator.repository.AppUserRepository;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;

    public CustomAuthenticationSuccessHandler(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        AppUser user = appUserRepository.findByUsername(authentication.getName())
                .orElse(null);

        if (user != null && user.isTwoFactorEnabled()) {
            response.sendRedirect("/2fa/verify/" + user.getId());
        } else {
            response.sendRedirect("/");
        }
    }
}
