package com.gameops.service;

import com.gameops.model.MatchResult;
import com.gameops.model.PlayerStats;
import com.gameops.repository.MatchResultRepository;
import com.gameops.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchService {

    @Autowired
    private MatchResultRepository matchResultRepository;

    @Autowired
    private PlayerStatsRepository playerStatsRepository;

    @Autowired
    private SuspiciousDetectionService suspiciousDetectionService;

    @Transactional
    public MatchResult saveMatchResult(MatchResult result) {
        // Save raw match result
        MatchResult saved = matchResultRepository.save(result);

        // Check if this match is suspicious
        boolean matchSuspicious = suspiciousDetectionService.isSuspicious(saved);

        // Update player stats
        updatePlayerStats(saved, matchSuspicious);

        return saved;
    }

    private void updatePlayerStats(MatchResult match, boolean matchSuspicious) {
        String playerId = match.getPlayerId();
        
        PlayerStats stats = playerStatsRepository.findById(playerId)
                .orElse(PlayerStats.builder()
                        .playerId(playerId)
                        .region(match.getRegion())
                        .totalMatches(0)
                        .totalScore(0)
                        .avgPing(0.0)
                        .avgKills(0.0)
                        .avgDeaths(0.0)
                        .skillRating(0)
                        .isSuspicious(false)
                        .seasonId(1)
                        .build());

        // Recalculate fields
        stats.setRegion(match.getRegion()); // Update region in case it changed
        int newTotalMatches = stats.getTotalMatches() + 1;
        stats.setTotalMatches(newTotalMatches);
        
        int newTotalScore = stats.getTotalScore() + match.getScore();
        stats.setTotalScore(newTotalScore);

        double newAvgPing = ((stats.getAvgPing() * (newTotalMatches - 1)) + match.getPing()) / newTotalMatches;
        stats.setAvgPing(newAvgPing);

        double newAvgKills = ((stats.getAvgKills() * (newTotalMatches - 1)) + match.getKills()) / newTotalMatches;
        stats.setAvgKills(newAvgKills);

        double newAvgDeaths = ((stats.getAvgDeaths() * (newTotalMatches - 1)) + match.getDeaths()) / newTotalMatches;
        stats.setAvgDeaths(newAvgDeaths);

        if (matchSuspicious) {
            stats.setSuspicious(true);
        }

        // Calculate skill rating:
        // Skill Rating = (Avg Score * 0.5) + (KD Ratio * 100) + (Total Matches * 5)
        double avgScore = (double) newTotalScore / newTotalMatches;
        double kdRatio = newAvgDeaths > 0 ? newAvgKills / newAvgDeaths : newAvgKills;
        double skill = (avgScore * 0.5) + (kdRatio * 100.0) + (newTotalMatches * 5.0);
        stats.setSkillRating((int) skill);

        playerStatsRepository.save(stats);
    }
}
