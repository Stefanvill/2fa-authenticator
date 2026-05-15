package se.iths.stefan._faauthenticator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.iths.stefan._faauthenticator.model.AppUser;
import se.iths.stefan._faauthenticator.service.AppUserService;

@Controller
@RequestMapping
public class AppUserController {
    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register-form";
    }

    @PostMapping("/register")
    public String register(Model model, AppUser appUser) {
        model.addAttribute("user", service.createUser(appUser));
        return "redirect:/";
    }
}
