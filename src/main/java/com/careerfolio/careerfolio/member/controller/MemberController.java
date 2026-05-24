package com.careerfolio.careerfolio.member.controller;

import com.careerfolio.careerfolio.member.dto.MemberDto;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.service.FavoriteService; // FavoriteService 추가
import com.careerfolio.careerfolio.member.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final FavoriteService favoriteService; // FavoriteService 주입


    @GetMapping("/member/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signupSubmit(
            @Valid MemberDto memberDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        if (!memberDto.getPassword1().equals(memberDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
            return "member/signup";
        }

        try {
            memberService.create(memberDto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/signup";
        }

        try {
            request.login(memberDto.getUsername(), memberDto.getPassword1());
        } catch (ServletException e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }



    @GetMapping("/member/login")
    public String loginForm() {
        return "member/login";
    }


    @GetMapping("/member/mypage")
    public String myPage(Model model, Principal principal) {

        if (principal == null)
            return "redirect:/member/login";

        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);

        // 찜한 포트폴리오 목록 추가
        model.addAttribute("favoriteList", favoriteService.getFavoritesByUsername(principal.getName()));

        return "member/mypage";
    }


    @GetMapping("/member/edit")
    public String editProfileForm(Model model, Principal principal) {

        if (principal == null)
            return "redirect:/member/login";

        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);

        return "member/edit";
    }

    @PostMapping("/member/edit")
    public String editProfileSubmit(
            Principal principal,
            String name,
            String email,
            Model model
    ) {

        if (principal == null)
            return "redirect:/member/login";

        try {
            memberService.updateProfile(principal.getName(), name, email);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "member/edit";
        }

        return "redirect:/member/mypage";
    }


    @GetMapping("/member/password")
    public String passwordForm(Principal principal) {

        if (principal == null)
            return "redirect:/member/login";

        return "member/password";
    }

    @PostMapping("/member/password")
    public String changePassword(
            Principal principal,
            String oldPassword,
            String newPassword1,
            String newPassword2,
            Model model
    ) {
        if (principal == null)
            return "redirect:/member/login";

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


    @GetMapping("/member/favorites")
    public String favoritesPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);

        model.addAttribute("favoriteList", favoriteService.getFavoritesByUsername(principal.getName()));

        return "member/favorites";  // favorites.html로 리턴
    }


    @PostMapping("/member/favorites/save")
    public String saveFavorite(Model model, Principal principal, Long portfolioId) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        boolean success = favoriteService.addFavorite(principal.getName(), portfolioId);
        if (success) {
            model.addAttribute("message", "포트폴리오가 찜 목록에 추가되었습니다.");
        } else {
            model.addAttribute("error", "이미 찜한 포트폴리오입니다.");
        }

        return "redirect:/portfolio/detail/" + portfolioId;
    }


    @PostMapping("/member/favorites/remove")
    public String removeFavorite(Model model, Principal principal, Long portfolioId) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        boolean success = favoriteService.removeFavorite(principal.getName(), portfolioId);
        if (success) {
            model.addAttribute("message", "포트폴리오가 찜 목록에서 제거되었습니다.");
        } else {
            model.addAttribute("error", "저장된 포트폴리오가 없습니다.");
        }

        return "redirect:/member/favorites";
    }

    @PostMapping("/member/delete")
    public String deleteMember(
            Principal principal,
            HttpServletRequest request
    ) {
        if (principal == null)
            return "redirect:/member/login";


        memberService.deleteAllByUsername(principal.getName());

        try {
            request.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }

}
