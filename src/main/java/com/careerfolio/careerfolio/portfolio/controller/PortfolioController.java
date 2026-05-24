package com.careerfolio.careerfolio.portfolio.controller;

import com.careerfolio.careerfolio.comment.service.CommentService;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.service.PortfolioService;
import com.careerfolio.careerfolio.member.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final FavoriteService favoriteService;
    private final CommentService commentService;

    // 공개 포트폴리오 리스트
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            Model model,
            Principal principal
    ) {
        String username = (principal != null) ? principal.getName() : null;

        if (sort == null) sort = "latest";

        List<Portfolio> list = portfolioService.getSortedPublicPortfolios(sort, keyword);

        model.addAttribute("portfolioList", list);
        model.addAttribute("username", username);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword);

        return "portfolio/list";
    }


    @GetMapping("/create")
    public String createForm(Principal principal) {
        if (principal == null) return "redirect:/member/login";
        return "portfolio/create";
    }


    @PostMapping("/create")
    public String create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean publicState,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String githubUrl,
            @RequestParam(required = false) String deployUrl,
            Principal principal
    ) throws Exception {


        if (principal == null) return "redirect:/member/login";

        Portfolio saved = portfolioService.createPortfolio(
                principal.getName(),
                title,
                content,
                publicState,
                file,
                githubUrl,
                deployUrl
        );

        return "redirect:/portfolio/detail/" + saved.getId();
    }

    @GetMapping("/my")
    public String myList(Model model, Principal principal) {

        if (principal == null) return "redirect:/member/login";

        String username = principal.getName();

        model.addAttribute("portfolioList", portfolioService.getMyPortfolios(username));
        model.addAttribute("username", username);

        return "portfolio/my";
    }

    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {

        String username = (principal != null) ? principal.getName() : null;

        Portfolio p = portfolioService.getOne(id);

        // 비공개 보호
        if (!p.isPublicState()) {
            if (principal == null || !p.getMember().getUsername().equals(username)) {
                return "redirect:/portfolio/list";
            }
        }

        portfolioService.increaseViews(id, username);

        model.addAttribute("portfolio", p);
        model.addAttribute("username", username);

        boolean isOwner = (principal != null &&
                p.getMember().getUsername().equals(username));
        model.addAttribute("isOwner", isOwner);

        boolean favoriteSaved = favoriteService.isFavorite(username, id);
        model.addAttribute("favoriteSaved", favoriteSaved);

        boolean alreadyLiked = portfolioService.isLikedByUser(id, username);
        model.addAttribute("alreadyLiked", alreadyLiked);

        model.addAttribute("comments", commentService.getComments(p));

        return "portfolio/detail";
    }


    @PostMapping("/like/{id}")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            Principal principal
    ) {

        if (principal == null) {
            return ResponseEntity.status(403)
                    .body("{\"error\": \"NOT_LOGIN\"}");
        }

        String username = principal.getName();
        String result = portfolioService.toggleLike(id, username);
        int likeCount = portfolioService.getLikeCount(id);

        return ResponseEntity.ok(
                "{ \"result\": \"" + result + "\", \"likeCount\": " + likeCount + " }"
        );
    }


    @PostMapping("/toggleFavorite/{id}")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            Principal principal
    ) {

        if (principal == null) {
            return ResponseEntity.status(403)
                    .body("{\"error\": \"NOT_LOGIN\"}");
        }

        String username = principal.getName();

        boolean isSaved = favoriteService.isFavorite(username, id);

        if (isSaved) {
            favoriteService.removeFavorite(username, id);
            return ResponseEntity.ok("{\"saved\": false}");
        }

        favoriteService.addFavorite(username, id);
        return ResponseEntity.ok("{\"saved\": true}");
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        if (principal == null) return "redirect:/member/login";

        Portfolio p = portfolioService.getOne(id);

        if (!p.getMember().getUsername().equals(principal.getName())) {
            return "redirect:/portfolio/list";
        }

        model.addAttribute("portfolio", p);

        return "portfolio/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean publicState,
            @RequestParam(required = false) String githubUrl,
            @RequestParam(required = false) String deployUrl,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "deleteFile", defaultValue = "false") boolean deleteFile,
            Principal principal
    ) throws Exception {

        if (principal == null) return "redirect:/member/login";

        Portfolio portfolio = portfolioService.getOne(id);

        if (deleteFile) {
            portfolioService.deletePortfolioFile(portfolio);
        }

        if (file != null && !file.isEmpty()) {
            portfolioService.updatePortfolioFile(portfolio, file);
        }

        portfolioService.updateTextFields(
                portfolio, title, content, publicState, githubUrl, deployUrl
        );

        return "redirect:/portfolio/detail/" + id;
    }


    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) return "redirect:/member/login";

        portfolioService.deletePortfolio(id, principal.getName());

        return "redirect:/portfolio/my";
    }
}
