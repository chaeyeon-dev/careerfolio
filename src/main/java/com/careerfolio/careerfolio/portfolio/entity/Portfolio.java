package com.careerfolio.careerfolio.portfolio.entity;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.comment.entity.Comment;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private String fileUrl;

    private String thumbnailUrl;

    private boolean publicState;

    private int views;

    private int likeCount;

    private LocalDateTime createdAt;

    private String authorName;

    @Column(nullable = false)
    private boolean deleted = false;

    @ManyToOne
    private Member member;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private String githubUrl;
    private String deployUrl;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.member != null) {
            this.authorName = this.member.getName();
        }
    }
}
