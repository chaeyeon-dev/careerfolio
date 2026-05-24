package com.careerfolio.careerfolio.comment.repository;

import com.careerfolio.careerfolio.comment.entity.Comment;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 포트폴리오별 댓글 목록 불러오기
    List<Comment> findByPortfolioOrderByCreatedAtAsc(Portfolio portfolio);

    void deleteByMember(Member member);
}