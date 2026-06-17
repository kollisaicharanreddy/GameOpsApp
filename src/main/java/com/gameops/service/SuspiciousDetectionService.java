package com.gameops.service;

import com.gameops.model.MatchResult;
import com.gameops.repository.MatchResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SuspiciousDetectionService {

    @Autowired
    private MatchResultRepository matchResultRepository;

    public boolean isSuspicious(MatchResult match) {
        int suspicionScore = 0;

        // Rule 1: Impossible Score Rate
        if (match.getMatchDurationSeconds() > 0) {
            double scorePerMinute = match.getScore() / (match.getMatchDurationSeconds() / 60.0);
            if (scorePerMinute > 10000) {
                suspicionScore += 50;
            }
        }

        // Rule 2: Unrealistic Kills
        if (match.getKills() > 100 && match.getMatchDurationSeconds() < 120) {
            suspicionScore += 30;
        }

        // Rule 3: Zero Death Abuse
        if (match.getKills() > 50 && match.getDeaths() == 0) {
            suspicionScore += 30;
        }

        // Rule 4: Historical Outlier
        List<MatchResult> previousMatches = matchResultRepository.findByPlayerId(match.getPlayerId());
        if (previousMatches != null && previousMatches.size() >= 3) {
            double avgScore = previousMatches.stream()
                    .mapToInt(MatchResult::getScore)
                    .average()
                    .orElse(0.0);
            if (match.getScore() > avgScore * 3.0) {
                suspicionScore += 40;
            }
        }

        // Rule 5: Ping/Region Mismatch / VPN Abuse
        // E.g., latency extremely high (e.g. > 300 ms) indicates possible location spoofing or VPN abuse
        if (match.getPing() > 300) {
            suspicionScore += 20;
        }

        return suspicionScore > 70;
    }
}