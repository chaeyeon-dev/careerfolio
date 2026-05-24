package com.careerfolio.careerfolio.member.repository;

import com.careerfolio.careerfolio.member.entity.Favorite;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByMemberAndPortfolio(Member member, Portfolio portfolio);

    List<Favorite> findByMember(Member member);

    boolean existsByMemberAndPortfolio(Member member, Portfolio portfolio);

    void deleteByPortfolio(Portfolio portfolio);

    void deleteByMember(Member member);

    void deleteByPortfolio_Id(Long portfolioId);
}
