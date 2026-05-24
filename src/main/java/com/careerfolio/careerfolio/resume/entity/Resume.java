package com.careerfolio.careerfolio.resume.entity;

import com.careerfolio.careerfolio.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String name;
    private String email;
    private String phone;
    private String birthday;
    private String address;

    private String photoPath;

    @Lob
    private String skills;

    @Lob
    private String certificates;

    @Lob
    private String educations;

    @Lob
    private String growth;

    @Lob
    private String motivation;

    @Lob
    private String personality;

    @Lob
    private String future;

    @ManyToOne
    private Member member;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;
}
