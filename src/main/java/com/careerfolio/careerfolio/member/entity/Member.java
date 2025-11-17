package com.careerfolio.careerfolio.member.entity;

import com.careerfolio.careerfolio.member.constant.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    // ⭐ 가입일 자동 저장
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ⭐ 프로필 수정일 자동 저장 (선택)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
