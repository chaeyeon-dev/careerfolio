package com.careerfolio.careerfolio.portfolio.service;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;

    // ===============================
    // 내가 작성한 포트폴리오 목록 조회
    // ===============================
    public List<Portfolio> getMyPortfolios(String username) {
        return portfolioRepository.findByMember_Username(username);
    }

    // 공개 포트폴리오 조회
    public List<Portfolio> getPublicPortfolios() {
        return portfolioRepository.findByIsPublicTrue();
    }

    // 포트폴리오 1개 조회
    public Portfolio getOne(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다."));
    }

    // ===============================
    // 🔥 포트폴리오 생성 (썸네일 포함)
    // ===============================
    public void create(String username,
                       String title,
                       String content,
                       boolean publicState,
                       MultipartFile thumbnailFile) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        String thumbnailUrl = uploadThumbnail(thumbnailFile);

        Portfolio portfolio = Portfolio.builder()
                .title(title)
                .content(content)
                .isPublic(publicState)
                .thumbnailUrl(thumbnailUrl)
                .member(member)
                .build();

        portfolioRepository.save(portfolio);
    }

    // ===============================
    // 🔥 포트폴리오 수정 (썸네일도 수정 가능)
    // ===============================
    public void update(Long id,
                       String title,
                       String content,
                       boolean publicState,
                       String username,
                       MultipartFile thumbnailFile) {

        Portfolio p = getOne(id);

        // 권한 체크
        if (!p.getMember().getUsername().equals(username)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 텍스트 수정
        p.setTitle(title);
        p.setContent(content);
        p.setPublic(publicState);

        // 🔥 새 썸네일이 업로드된 경우에만 갱신
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String newThumbUrl = uploadThumbnail(thumbnailFile);
            p.setThumbnailUrl(newThumbUrl);
        }

        portfolioRepository.save(p);
    }

    // ===============================
    // 삭제
    // ===============================
    public void delete(Long id, String username) {

        Portfolio p = getOne(id);

        if (!p.getMember().getUsername().equals(username)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        portfolioRepository.delete(p);
    }

    // ===============================
    // 조회수 증가
    // ===============================
    public void increaseViews(Portfolio portfolio) {
        portfolio.setViews(portfolio.getViews() + 1);
        portfolioRepository.save(portfolio);
    }

    // ===============================
    // 🔥 공통: 썸네일 업로드 기능
    // ===============================
    private String uploadThumbnail(MultipartFile thumbnailFile) {

        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            return null;
        }

        try {
            String uploadDir = "C:/careerfolio/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
            File saveFile = new File(uploadDir + fileName);

            thumbnailFile.transferTo(saveFile);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("썸네일 업로드 실패: " + e.getMessage());
        }
    }
}
