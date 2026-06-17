package com.gameops.service;

import com.gameops.model.PlayerStats;
import com.gameops.repository.MatchResultRepository;
import com.gameops.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private PlayerStatsRepository playerStatsRepository;

    @Autowired
    private MatchResultRepository matchResultRepository;

    public List<PlayerStats> getGlobalLeaderboard() {
        List<PlayerStats> players = playerStatsRepository.findByIsSuspiciousFalseOrderByTotalScoreDescAvgDeathsAsc();
        return sortLeaderboard(players);
    }

    public List<PlayerStats> getRegionalLeaderboard(String region) {
        List<PlayerStats> players = playerStatsRepository.findByRegionAndIsSuspiciousFalseOrderByTotalScoreDescAvgDeathsAsc(region);
        return sortLeaderboard(players);
    }

    @Transactional
    public void resetLeaderboard() {
        // Archive/Reset logic: Clear current match results and reset player stats for new season
        matchResultRepository.deleteAll();
        playerStatsRepository.deleteAll();
    }

    private List<PlayerStats> sortLeaderboard(List<PlayerStats> players) {
        return players.stream()
                .sorted((p1, p2) -> {
                    // Rule 1: Higher score wins
                    int scoreCompare = Integer.compare(p2.getTotalScore(), p1.getTotalScore());
                    if (scoreCompare != 0) return scoreCompare;

                    // Rule 2: Lower average deaths wins
                    int deathCompare = Double.compare(p1.getAvgDeaths(), p2.getAvgDeaths());
                    if (deathCompare != 0) return deathCompare;

                    // Rule 3: Higher KD ratio wins
                    double kd1 = p1.getAvgDeaths() > 0 ? p1.getAvgKills() / p1.getAvgDeaths() : p1.getAvgKills();
                    double kd2 = p2.getAvgDeaths() > 0 ? p2.getAvgKills() / p2.getAvgDeaths() : p2.getAvgKills();
                    int kdCompare = Double.compare(kd2, kd1);
                    if (kdCompare != 0) return kdCompare;

                    // Rule 4: Alphabetic Player ID fallback for deterministic sorting
                    return p1.getPlayerId().compareTo(p2.getPlayerId());
                })
                .collect(Collectors.toList());
    }
}
