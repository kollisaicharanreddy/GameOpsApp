package com.gameops.controller;

import com.gameops.model.PlayerStats;
import com.gameops.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping("/global")
    public ResponseEntity<List<PlayerStats>> getGlobalLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/{region}")
    public ResponseEntity<List<PlayerStats>> getRegionalLeaderboard(@PathVariable String region) {
        return ResponseEntity.ok(leaderboardService.getRegionalLeaderboard(region));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetLeaderboard() {
        leaderboardService.resetLeaderboard();
        return ResponseEntity.ok("Leaderboard has been reset for the new season");
    }
}
