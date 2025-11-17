package com.careerfolio.careerfolio.member.controller;

import com.careerfolio.careerfolio.member.dto.MemberDto;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ------------------------------
    // 회원가입 화면
    // ------------------------------
    @GetMapping("/member/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "member/signup";
    }

    // ------------------------------
    // 회원가입 처리
    // ------------------------------
    @PostMapping("/member/signup")
    public String signupSubmit(
            @Valid MemberDto memberDto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        if (!memberDto.getPassword1().equals(memberDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
            return "member/signup";
        }

        memberService.create(memberDto);
        return "redirect:/";
    }

    // ------------------------------
    // 로그인 화면
    // ------------------------------
    @GetMapping("/member/login")
    public String loginForm() {
        return "member/login";
    }

    // ------------------------------
    // 마이페이지
    // ------------------------------
    @GetMapping("/member/mypage")
    public String myPage(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/member/login"; // 비로그인 접근 방지
        }

        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);

        return "member/mypage";
    }

    // ------------------------------
    // 비밀번호 변경 화면
    // ------------------------------
    @GetMapping("/member/password")
    public String passwordForm() {
        return "member/password";
    }

    // ------------------------------
    // 비밀번호 변경 처리
    // ------------------------------
    @PostMapping("/member/password")
    public String changePassword(
            Principal principal,
            String oldPassword,
            String newPassword1,
            String newPassword2,
            Model model
    ) {
        if (!newPassword1.equals(newPassword2)) {
            model.addAttribute("error", "새 비밀번호가 서로 다릅니다.");
            return "member/password";
        }

        try {
            memberService.changePassword(principal.getName(), oldPassword, newPassword1);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "member/password";
        }

        return "redirect:/member/mypage";
    }
}
