package com.careerfolio.careerfolio.comment.controller;

import com.careerfolio.careerfolio.comment.entity.Comment;
import com.careerfolio.careerfolio.comment.service.CommentService;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.service.MemberService;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class CommentController {

    private final CommentService commentService;
    private final PortfolioService portfolioService;
    private final MemberService memberService;

    // 댓글 등록
    @PostMapping("/{portfolioId}/comments")
    public Map<String, String> create(
            @PathVariable Long portfolioId,
            @RequestBody Map<String, String> body,
            Principal principal) {

        if (principal == null) {
            return Map.of("result", "not_login");
        }

        Member member = memberService.getMember(principal.getName());
        Portfolio portfolio = portfolioService.getOne(portfolioId);

        String content = body.get("content");

        commentService.create(portfolio, member, content);

        return Map.of("result", "ok");
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{id}")
    public Map<String, String> delete(
            @PathVariable Long id,
            Principal principal) {

        if (principal == null) {
            return Map.of("result", "not_login");
        }

        Comment comment = commentService.findById(id);

        if (!comment.getMember().getUsername().equals(principal.getName())) {
            return Map.of("result", "forbidden");
        }

        commentService.delete(comment);
        return Map.of("result", "ok");
    }


    // 댓글 수정
    @PutMapping("/comments/{id}")
    public Map<String, String> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {

        if (principal == null) {
            return Map.of("result", "not_login");
        }

        Comment comment = commentService.findById(id);

        if (!comment.getMember().getUsername().equals(principal.getName())) {
            return Map.of("result", "forbidden");
        }

        commentService.update(comment, body.get("content"));
        return Map.of("result", "ok");
    }
}