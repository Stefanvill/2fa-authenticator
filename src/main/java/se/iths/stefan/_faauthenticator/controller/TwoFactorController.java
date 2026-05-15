package se.iths.stefan._faauthenticator.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.iths.stefan._faauthenticator.model.AppUser;
import se.iths.stefan._faauthenticator.repository.AppUserRepository;
import se.iths.stefan._faauthenticator.service.AppUserService;

@Controller
@RequestMapping("/2fa")
public class TwoFactorController {
    private final AppUserService appUserService;
    private final AppUserRepository repository;

    public TwoFactorController(AppUserService appUserService, AppUserRepository repository) {
        this.appUserService = appUserService;
        this.repository = repository;
    }

    @GetMapping
    public String tfaHomePage(Model model, Authentication auth) {
        AppUser user = repository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + auth.getName()));

        model.addAttribute("user", user);
        return "2fa-home-page";
    }

    @GetMapping({"/setup", "/setup/"})
    public String showSetup(Authentication auth, Model model) {
        AppUser currentUser = repository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Sätt 2FA enabled och generera secret
        currentUser = appUserService.setupTwoFactor(currentUser.getId());

        String qrUri = "otpauth://totp/2fa-authenticator:" + currentUser.getUsername()
                + "?secret=" + currentUser.getTwoFactorSecret()
                + "&issuer=2fa-authenticator";

        model.addAttribute("user", currentUser);
        model.addAttribute("qrUri", qrUri);
        return "twofactor-setup";
    }


    @PostMapping("/verify")
    public String verifyCode(@RequestParam Long userId,
                             @RequestParam int code,
                             Model model) {
        boolean valid = appUserService.verifyTwoFactorCode(userId, code);

        if (!valid) {
            model.addAttribute("error", "Invalid authentication code");
            model.addAttribute("userId", userId);
            return "twofactor-verify";
        }

        // Mark 2FA as enabled in database
        AppUser user = appUserService.enableTwoFactor(userId);

        // Grant full authentication
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return "redirect:/";
    }


    @GetMapping("/verify/{userId}")
    public String showVerify(@PathVariable Long userId, Model model) {
        model.addAttribute("userId", userId);
        return "twofactor-verify";
    }

}
