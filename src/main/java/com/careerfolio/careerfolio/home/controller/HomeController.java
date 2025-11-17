package com.careerfolio.careerfolio.home.controller;

import com.careerfolio.careerfolio.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String home(Model model) {
        String welcome = homeService.getWelcomeMessage();
        model.addAttribute("welcome", welcome);
        return "home/index"; // templates/home/index.html
    }
}
