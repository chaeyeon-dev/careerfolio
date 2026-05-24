package com.careerfolio.careerfolio.resume.repository;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByMember_Username(String username);


    void deleteByMember(Member member);
}
