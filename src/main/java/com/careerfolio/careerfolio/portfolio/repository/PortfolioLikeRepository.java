package com.careerfolio.careerfolio.portfolio.repository;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.entity.PortfolioLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioLikeRepository extends JpaRepository<PortfolioLike, Long> {

    PortfolioLike findByMemberAndPortfolio(Member member, Portfolio portfolio);

    int countByPortfolio_Id(Long portfolioId);

    void deleteByMemberAndPortfolio(Member member, Portfolio portfolio);

    void deleteByPortfolio(Portfolio portfolio);

    void deleteByMember(Member member);
}
