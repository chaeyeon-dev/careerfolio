package com.careerfolio.careerfolio.portfolio.controller;

import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    // ===============================
    // 내 포트폴리오 목록 보기
    // ===============================
    @GetMapping("/my")
    public String myList(Model model, Principal principal) {

        model.addAttribute("portfolioList",
                portfolioService.getMyPortfolios(principal.getName()));

        return "portfolio/list";
    }

    // ===============================
    // 작성 페이지
    // ===============================
    @GetMapping("/create")
    public String createForm() {
        return "portfolio/create";
    }

    // ===============================
    // 작성 처리 (썸네일 + PDF)
    // ===============================
    @PostMapping("/create")
    public String create(
            Principal principal,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile
    ) {

        portfolioService.create(
                principal.getName(),
                title,
                content,
                isPublic,
                thumbnailFile,
                pdfFile
        );

        return "redirect:/portfolio/my";
    }


    // ===============================
    // 상세보기
    // ===============================
    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {

        if (principal == null) {
            return "redirect:/member/login";
        }

        Portfolio portfolio = portfolioService.getOne(id);

        // 🔥 비공개 + 작성자 아님 = 접근 불가
        if (!portfolio.isPublic() &&
                !portfolio.getMember().getUsername().equals(principal.getName())) {
            return "redirect:/portfolio/my";
        }

        model.addAttribute("portfolio", portfolio);
        return "portfolio/detail";
    }

    // ===============================
    // 수정 페이지
    // ===============================
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {
        if (principal == null) return "redirect:/member/login";

        Portfolio portfolio = portfolioService.getOne(id);

        if (!portfolio.getMember().getUsername().equals(principal.getName())) {
            return "redirect:/portfolio/detail/" + id;
        }

        model.addAttribute("portfolio", portfolio);
        return "portfolio/edit";
    }

    // ===============================
    // 수정 처리 (썸네일 + PDF 수정 가능)
    // ===============================
    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            Principal principal
    ) {

        portfolioService.update(
                id,
                title,
                content,
                isPublic,
                principal.getName(),
                thumbnailFile,
                pdfFile
        );

        return "redirect:/portfolio/detail/" + id;
    }

    // ===============================
    // 삭제
    // ===============================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Principal principal
    ) {
        portfolioService.delete(id, principal.getName());
        return "redirect:/portfolio/my";
    }
}
