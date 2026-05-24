package com.careerfolio.careerfolio.home;

import com.careerfolio.careerfolio.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PortfolioService portfolioService;

    @GetMapping("/")
    public String home(Model model) {


        return "home/index";
    }
}
