package com.careerfolio.careerfolio.home.service;

import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getWelcomeMessage() {
        return "포트폴리오와 이력서를 한 곳에서 편하게 관리하세요.";
    }
}
