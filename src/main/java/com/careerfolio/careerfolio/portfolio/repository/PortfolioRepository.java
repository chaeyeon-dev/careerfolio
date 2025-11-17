package com.careerfolio.careerfolio.portfolio.repository;

import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // 🔥 내 포트폴리오 목록 (작성자 기준)
    List<Portfolio> findByMember_Username(String username);

    // 🔥 공개된 포트폴리오 목록
    List<Portfolio> findByIsPublicTrue();

    // 🔥 특정 유저의 공개된 포트폴리오만 조회 (필요한 경우)
    List<Portfolio> findByMember_UsernameAndIsPublicTrue(String username);
}
