package com.gameops.controller;

import com.gameops.model.PlayerStats;
import com.gameops.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@RestController
public class SuspiciousController {

    @Autowired
    private PlayerStatsRepository playerStatsRepository;

    @GetMapping("/flagged-players")
    public ResponseEntity<List<PlayerStats>> getFlaggedPlayers() {
        List<PlayerStats> flagged = playerStatsRepository.findAll().stream()
                .filter(PlayerStats::isSuspicious)
                .toList();
        return ResponseEntity.ok(flagged);
    }

    @PostMapping("/players/{playerId}/flag")
    public ResponseEntity<String> updatePlayerFlag(
            @PathVariable String playerId,
            @RequestParam(defaultValue = "true") boolean isSuspicious) {
        
        Optional<PlayerStats> statsOpt = playerStatsRepository.findById(playerId);
        if (statsOpt.isPresent()) {
            PlayerStats stats = statsOpt.get();
            stats.setSuspicious(isSuspicious);
            playerStatsRepository.save(stats);
            return ResponseEntity.ok("Player " + playerId + " suspension flag set to: " + isSuspicious);
        }
        
        return ResponseEntity.notFound().build();
    }
}
