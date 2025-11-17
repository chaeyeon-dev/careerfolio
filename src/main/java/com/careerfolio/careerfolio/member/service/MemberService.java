package com.careerfolio.careerfolio.member.service;

import com.careerfolio.careerfolio.member.constant.MemberRole;
import com.careerfolio.careerfolio.member.dto.MemberDto;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // ------------------------------
    // 회원 생성
    // ------------------------------
    public void create(MemberDto memberDto) {

        Member member = Member.builder()
                .username(memberDto.getUsername())
                .password(passwordEncoder.encode(memberDto.getPassword1()))
                .email(memberDto.getEmail())
                .role(MemberRole.USER)
                .build();

        memberRepository.save(member);
    }

    // ------------------------------
    // username 으로 회원 조회
    // ------------------------------
    public Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // ------------------------------
    // 비밀번호 변경
    // ------------------------------
    public void changePassword(String username, String oldPassword, String newPassword) {

        Member member = getMember(username);

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
}
