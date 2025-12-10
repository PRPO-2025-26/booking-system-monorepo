package si.fri.prpo.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Auth Service! 🚀";
    }

    @GetMapping("/health")
    public String health() {
        return "Auth Service is running!";
    }
}
