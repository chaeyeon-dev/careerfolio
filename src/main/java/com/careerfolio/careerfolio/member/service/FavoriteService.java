package com.careerfolio.careerfolio.member.service;

import com.careerfolio.careerfolio.member.entity.Favorite;
import com.careerfolio.careerfolio.member.entity.Member;
import com.careerfolio.careerfolio.portfolio.entity.Portfolio;
import com.careerfolio.careerfolio.member.repository.FavoriteRepository;
import com.careerfolio.careerfolio.member.repository.MemberRepository;
import com.careerfolio.careerfolio.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
    private final PortfolioRepository portfolioRepository;

    public List<Favorite> getFavoritesByMember(Member member) {
        return favoriteRepository.findByMember(member);
    }

    public List<Favorite> getFavoritesByUsername(String username) {
        return memberRepository.findByUsername(username)
                .map(favoriteRepository::findByMember)
                .orElseGet(() -> List.of());
    }

    public boolean addFavorite(String username, Long portfolioId) {

        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) {
            return false;
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) {
            return false;
        }

        if (favoriteRepository.existsByMemberAndPortfolio(member, portfolio)) {
            return false; // 이미 존재
        }

        Favorite favorite = new Favorite();
        favorite.setMember(member);
        favorite.setPortfolio(portfolio);

        favoriteRepository.save(favorite);
        return true;
    }

    public boolean removeFavorite(String username, Long portfolioId) {

        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) {
            return false;
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) {
            return false;
        }

        Optional<Favorite> favorite = favoriteRepository.findByMemberAndPortfolio(member, portfolio);
        if (favorite.isEmpty()) {
            return false;
        }

        favoriteRepository.delete(favorite.get());
        return true;
    }

    public boolean isFavorite(String username, Long portfolioId) {

        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) {
            return false;
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) {
            return false;
        }

        return favoriteRepository.existsByMemberAndPortfolio(member, portfolio);
    }
}
