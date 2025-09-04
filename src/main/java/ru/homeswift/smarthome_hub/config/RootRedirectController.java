package ru.homeswift.smarthome_hub.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui/index.html";
    }
}