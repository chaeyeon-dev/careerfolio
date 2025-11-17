package com.careerfolio.careerfolio.portfolio.entity;

import com.careerfolio.careerfolio.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 🔥 작성자 (Member 엔티티와 연결)
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 🔥 공개 여부 (true = 공개, false = 비공개)
    @Builder.Default
    @Column(nullable = false)
    private boolean isPublic = false;

    // 🔥 조회수
    @Builder.Default
    @Column(nullable = false)
    private int views = 0;

    // 🔥 썸네일 이미지 경로 (추가됨!)
    private String thumbnailUrl;
    private String pdfUrl;
}
