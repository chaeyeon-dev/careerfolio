package com.careerfolio.careerfolio.portfolio.service;

import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.member.repository.FavoriteRepository;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.portfolio.entity.PortfolioLike;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioLikeRepository;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final PortfolioLikeRepository likeRepository;
    private final String FILE_PATH = "C:/careerfolio/uploads/portfolio/";

    public Portfolio getOne(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다."));
    }

    public List<Portfolio> getMyPortfolios(String username) {
        return portfolioRepository.findByMember_Username(username);
    }

    public List<Portfolio> getSortedPublicPortfolios(String sort, String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return switch (sort == null ? "latest" : sort) {
                case "views" -> portfolioRepository.findByPublicStateTrueOrderByViewsDesc();
                case "likes" -> portfolioRepository.findByPublicStateTrueOrderByLikeCountDesc();
                default -> portfolioRepository.findByPublicStateTrueOrderByIdDesc();
            };
        }

        keyword = "%" + keyword.toLowerCase() + "%";

        return switch (sort == null ? "latest" : sort) {
            case "views" -> portfolioRepository.searchPublicByViews(keyword);
            case "likes" -> portfolioRepository.searchPublicByLikes(keyword);
            default -> portfolioRepository.searchPublicLatest(keyword);
        };
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        String name = file.getOriginalFilename().toLowerCase();

        if (name.endsWith(".zip"))
            throw new IllegalArgumentException("ZIP 파일은 업로드할 수 없습니다.");
    }

    private String createPdfThumbnail(String pdfPath) {
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 120);

            String name = "thumb_" + UUID.randomUUID() + ".png";
            String fullPath = FILE_PATH + name;

            ImageIO.write(image, "png", new File(fullPath));
            document.close();

            return "/uploads/portfolio/" + name;
        } catch (Exception e) {
            return null;
        }
    }

    private String createImageThumbnail(String fullPath) {
        try {
            BufferedImage original = ImageIO.read(new File(fullPath));
            int width = 600;
            int height = (int)((double) original.getHeight() / original.getWidth() * width);

            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            resized.getGraphics().drawImage(original, 0, 0, width, height, null);

            String name = "thumb_" + UUID.randomUUID() + ".jpg";
            ImageIO.write(resized, "jpg", new File(FILE_PATH + name));

            return "/uploads/portfolio/" + name;
        } catch (Exception e) {
            return null;
        }
    }


    private String[] handleFileUpload(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return new String[]{null, null};

        validateFile(file);

        new File(FILE_PATH).mkdirs();

        String original = file.getOriginalFilename().toLowerCase();
        String savedName = UUID.randomUUID() + "_" + original;
        String fullPath = FILE_PATH + savedName;

        file.transferTo(new File(fullPath));

        String fileUrl = "/uploads/portfolio/" + savedName;
        String thumbnailUrl = null;

        if (original.endsWith(".pdf")) {
            thumbnailUrl = createPdfThumbnail(fullPath);
        } else if (original.endsWith(".png") || original.endsWith(".jpg") || original.endsWith(".jpeg")) {
            thumbnailUrl = createImageThumbnail(fullPath);
        }

        return new String[]{fileUrl, thumbnailUrl};
    }

    public Portfolio createPortfolio(
            String username,
            String title,
            String content,
            boolean publicState,
            MultipartFile file,
            String githubUrl,
            String deployUrl
    ) throws Exception {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 정보 없음"));

        String[] uploaded = handleFileUpload(file);

        Portfolio p = Portfolio.builder()
                .title(title)
                .content(content)
                .publicState(publicState)
                .fileUrl(uploaded[0])
                .thumbnailUrl(uploaded[1])
                .member(member)
                .authorName(member.getName())
                .views(0)
                .likeCount(0)
                .githubUrl(githubUrl)
                .deployUrl(deployUrl)
                .build();

        return portfolioRepository.save(p);
    }

    public void updatePortfolio(
            Long id,
            String username,
            String title,
            String content,
            boolean publicState,
            MultipartFile newFile
    ) throws Exception {

        Portfolio p = getOne(id);

        if (!p.getMember().getUsername().equals(username))
            throw new RuntimeException("수정 권한 없음");

        p.setTitle(title);
        p.setContent(content);
        p.setPublicState(publicState);

        if (newFile != null && !newFile.isEmpty()) {
            if (p.getFileUrl() != null)
                Files.deleteIfExists(Paths.get(p.getFileUrl().replace("/uploads/portfolio/", FILE_PATH)));

            if (p.getThumbnailUrl() != null)
                Files.deleteIfExists(Paths.get(p.getThumbnailUrl().replace("/uploads/portfolio/", FILE_PATH)));

            String[] uploaded = handleFileUpload(newFile);
            p.setFileUrl(uploaded[0]);
            p.setThumbnailUrl(uploaded[1]);
        }

        portfolioRepository.save(p);
    }


    @Transactional
    public void deletePortfolio(Long id, String username) {

        Portfolio p = getOne(id);

        if (!p.getMember().getUsername().equals(username)) {
            throw new RuntimeException("삭제 권한 없음");
        }

        favoriteRepository.deleteByPortfolio(p);

        likeRepository.deleteAll(
                likeRepository.findAll().stream()
                        .filter(l -> l.getPortfolio().getId().equals(id))
                        .toList()
        );

        try {
            if (p.getFileUrl() != null)
                Files.deleteIfExists(Paths.get(p.getFileUrl().replace("/uploads/portfolio/", FILE_PATH)));

            if (p.getThumbnailUrl() != null)
                Files.deleteIfExists(Paths.get(p.getThumbnailUrl().replace("/uploads/portfolio/", FILE_PATH)));
        } catch (Exception ignored) {}

        portfolioRepository.delete(p);
    }


    public void increaseViews(Long id, String username) {
        Portfolio p = getOne(id);

        if (username == null) return;
        if (p.getMember().getUsername().equals(username)) return;

        p.setViews(p.getViews() + 1);
        portfolioRepository.save(p);
    }


    public String toggleLike(Long portfolioId, String username) {

        Portfolio p = getOne(portfolioId);

        if (p.getMember().getUsername().equals(username))
            return "forbidden";

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        PortfolioLike like = likeRepository.findByMemberAndPortfolio(member, p);

        if (like != null) {
            likeRepository.delete(like);
        } else {
            likeRepository.save(
                    PortfolioLike.builder()
                            .member(member)
                            .portfolio(p)
                            .build()
            );
        }

        int newCount = likeRepository.countByPortfolio_Id(portfolioId);
        p.setLikeCount(newCount);

        portfolioRepository.save(p);
        portfolioRepository.flush();

        return (like != null) ? "unliked" : "liked";
    }

    public int getLikeCount(Long portfolioId) {
        return likeRepository.countByPortfolio_Id(portfolioId);
    }

    public boolean isLikedByUser(Long portfolioId, String username) {

        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) return false;

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) return false;

        return likeRepository.findByMemberAndPortfolio(member, portfolio) != null;
    }


    public void deletePortfolioFile(Portfolio portfolio) throws Exception {

        if (portfolio.getFileUrl() != null) {
            Files.deleteIfExists(
                    Paths.get(portfolio.getFileUrl().replace("/uploads/portfolio/", FILE_PATH))
            );
            portfolio.setFileUrl(null);
        }

        if (portfolio.getThumbnailUrl() != null) {
            Files.deleteIfExists(
                    Paths.get(portfolio.getThumbnailUrl().replace("/uploads/portfolio/", FILE_PATH))
            );
            portfolio.setThumbnailUrl(null);
        }

        portfolioRepository.save(portfolio);
    }


    public void updatePortfolioFile(Portfolio portfolio, MultipartFile newFile) throws Exception {

        deletePortfolioFile(portfolio);

        String[] uploaded = handleFileUpload(newFile);

        portfolio.setFileUrl(uploaded[0]);
        portfolio.setThumbnailUrl(uploaded[1]);

        portfolioRepository.save(portfolio);
    }


    public void updateTextFields(
            Portfolio portfolio,
            String title,
            String content,
            boolean publicState,
            String githubUrl,
            String deployUrl
    ) {
        portfolio.setTitle(title);
        portfolio.setContent(content);
        portfolio.setPublicState(publicState);
        portfolio.setGithubUrl(githubUrl);
        portfolio.setDeployUrl(deployUrl);

        portfolioRepository.save(portfolio);
    }
}