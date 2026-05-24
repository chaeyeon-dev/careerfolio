package com.careerfolio.careerfolio.comment.service;

import com.careerfolio.careerfolio.comment.entity.Comment;
import com.careerfolio.careerfolio.comment.repository.CommentRepository;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    // 댓글 작성
    public Comment create(Portfolio portfolio, Member member, String content) {
        Comment comment = Comment.builder()
                .portfolio(portfolio)
                .member(member)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    // 댓글 목록
    public List<Comment> getComments(Portfolio portfolio) {
        return commentRepository.findByPortfolioOrderByCreatedAtAsc(portfolio);
    }

    // 댓글 수정
    public void update(Comment comment, String newContent) {
        comment.setContent(newContent);
        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    // 특정 댓글 찾기
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}
