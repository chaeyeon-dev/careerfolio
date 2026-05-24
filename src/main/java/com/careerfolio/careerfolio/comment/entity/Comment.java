package com.careerfolio.careerfolio.comment.entity;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 댓글 작성 시간
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    // 어떤 포트폴리오에 달린 댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    private Portfolio portfolio;
}