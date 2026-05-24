package com.careerfolio.careerfolio.member.service;

import com.careerfolio.careerfolio.comment.repository.CommentRepository;
import com.careerfolio.careerfolio.member.dto.MemberDto;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.repository.FavoriteRepository;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioLikeRepository;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioRepository;
import com.careerfolio.careerfolio.resume.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final PortfolioRepository portfolioRepository;
    private final ResumeRepository resumeRepository;
    private final FavoriteRepository favoriteRepository;
    private final PortfolioLikeRepository portfolioLikeRepository;

    private final CommentRepository commentRepository;

    public void create(MemberDto memberDto) {

        if (memberRepository.findByUsername(memberDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (memberRepository.findByEmail(memberDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        Member member = Member.builder()
                .username(memberDto.getUsername())
                .name(memberDto.getName())
                .password(passwordEncoder.encode(memberDto.getPassword1()))
                .email(memberDto.getEmail())
                .build();

        memberRepository.save(member);
    }


    public Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }


    public void changePassword(String username, String oldPassword, String newPassword) {

        Member member = getMember(username);

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }


    public void updateProfile(String username, String name, String email) {

        Member member = getMember(username);

        if (name != null && !name.trim().isEmpty()) {
            member.setName(name);
        }

        if (email != null && !email.trim().isEmpty()) {
            member.setEmail(email);
        }

        memberRepository.save(member);
    }

    public boolean checkPassword(Member member, String rawPassword) {
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    @Transactional
    public void deleteAllByUsername(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        commentRepository.deleteByMember(member);

        portfolioLikeRepository.deleteByMember(member);

        List<Portfolio> portfolioList = portfolioRepository.findByMember_Username(username);

        for (Portfolio p : portfolioList) {
            portfolioLikeRepository.deleteByPortfolio(p);  // 좋아요 삭제
            favoriteRepository.deleteByPortfolio(p);        // 찜 삭제
        }

        portfolioRepository.deleteByMember(member);

        resumeRepository.deleteByMember(member);

        favoriteRepository.deleteByMember(member);

        memberRepository.delete(member);
    }
}
