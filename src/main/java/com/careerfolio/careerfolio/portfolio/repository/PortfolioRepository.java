package com.careerfolio.careerfolio.portfolio.repository;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByMember_Username(String username);

    List<Portfolio> findByPublicStateTrue();

    List<Portfolio> findByPublicStateTrueOrderByIdDesc();

    List<Portfolio> findByPublicStateTrueOrderByViewsDesc();

    List<Portfolio> findByPublicStateTrueOrderByLikeCountDesc();


        @Query("""
        SELECT p FROM Portfolio p
        WHERE p.publicState = true
          AND (LOWER(p.title) LIKE :keyword
               OR LOWER(p.member.name) LIKE :keyword)
        ORDER BY p.id DESC
    """)
        List<Portfolio> searchPublicLatest(@Param("keyword") String keyword);


        @Query("""
        SELECT p FROM Portfolio p
        WHERE p.publicState = true
          AND (LOWER(p.title) LIKE :keyword
               OR LOWER(p.member.name) LIKE :keyword)
        ORDER BY p.views DESC
    """)
        List<Portfolio> searchPublicByViews(@Param("keyword") String keyword);


        @Query("""
        SELECT p FROM Portfolio p
        WHERE p.publicState = true
          AND (LOWER(p.title) LIKE :keyword
               OR LOWER(p.member.name) LIKE :keyword)
        ORDER BY p.likeCount DESC
    """)
        List<Portfolio> searchPublicByLikes(@Param("keyword") String keyword);


    @Query("SELECT p FROM Portfolio p " +
            "WHERE p.publicState = true AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.member.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Portfolio> searchByTitleOrAuthor(@Param("keyword") String keyword);

    void deleteByMember(Member member);


}
