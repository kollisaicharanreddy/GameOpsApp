package com.gameops;

import com.gameops.model.MatchResult;
import com.gameops.model.PlayerStats;
import com.gameops.repository.MatchResultRepository;
import com.gameops.repository.PlayerStatsRepository;
import com.gameops.service.LeaderboardService;
import com.gameops.service.MatchmakingService;
import com.gameops.service.SuspiciousDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameOpsServiceTest {

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private PlayerStatsRepository playerStatsRepository;

    @InjectMocks
    private SuspiciousDetectionService suspiciousDetectionService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @InjectMocks
    private MatchmakingService matchmakingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuspiciousDetection_NormalPlayer() {
        MatchResult normal = MatchResult.builder()
                .playerId("P001")
                .matchId("M001")
                .region("India")
                .score(3000)
                .kills(15)
                .deaths(5)
                .ping(50)
                .matchDurationSeconds(420) // 7 min, score per min = 428
                .build();

        when(matchResultRepository.findByPlayerId("P001")).thenReturn(new ArrayList<>());
        boolean result = suspiciousDetectionService.isSuspicious(normal);
        assertFalse(result, "Normal player stats should not be flagged as suspicious");
    }

    @Test
    void testSuspiciousDetection_HighKillsAndLowDuration() {
        // Rule 2 check: kills > 100 && duration < 120 (30 pts)
        // Rule 1 check: score per min = 120000 > 10000 (50 pts)
        // Total points = 80 -> Suspicious
        MatchResult cheater = MatchResult.builder()
                .playerId("P003")
                .matchId("M002")
                .region("SEA")
                .score(120000)
                .kills(120)
                .deaths(0)
                .ping(90)
                .matchDurationSeconds(60)
                .build();

        when(matchResultRepository.findByPlayerId("P003")).thenReturn(new ArrayList<>());
        boolean result = suspiciousDetectionService.isSuspicious(cheater);
        assertTrue(result, "Cheater with high kills and short duration should be flagged");
    }

    @Test
    void testSuspiciousDetection_ZeroDeathAbuse() {
        // Rule 3 check: kills > 50 && deaths == 0 (30 pts)
        // Rule 1 check: score per min = 60000 / 6 mins = 10000 (0 pts, not > 10000)
        // Let's add Rule 4 check by mocking previous average of 3000, and this match is 60000 (> 3x average) (40 pts)
        // Total points = 30 + 40 = 70. Wait, threshold is > 70.
        // Let's make sure it scores over 70 by adding Rule 5: ping 350 (> 300) (20 pts) -> Total = 90
        MatchResult abuse = MatchResult.builder()
                .playerId("P007")
                .matchId("M007")
                .region("Europe")
                .score(60000)
                .kills(60)
                .deaths(0)
                .ping(350)
                .matchDurationSeconds(360)
                .build();

        List<MatchResult> prev = Arrays.asList(
                MatchResult.builder().playerId("P007").score(3000).build(),
                MatchResult.builder().playerId("P007").score(3000).build(),
                MatchResult.builder().playerId("P007").score(3000).build()
        );
        when(matchResultRepository.findByPlayerId("P007")).thenReturn(prev);
        boolean result = suspiciousDetectionService.isSuspicious(abuse);
        assertTrue(result, "Zero death abuser with high ping should be flagged");
    }

    @Test
    void testLeaderboardSortingAndExclusion() {
        PlayerStats p1 = PlayerStats.builder().playerId("P001").totalScore(3000).avgDeaths(5.0).avgKills(15.0).region("India").isSuspicious(false).build();
        PlayerStats p2 = PlayerStats.builder().playerId("P002").totalScore(3000).avgDeaths(3.0).avgKills(10.0).region("India").isSuspicious(false).build(); // Wins tie-break (lower deaths)
        PlayerStats p3 = PlayerStats.builder().playerId("P003").totalScore(99000).avgDeaths(0.0).avgKills(250.0).region("SEA").isSuspicious(true).build(); // Suspicious
        PlayerStats p4 = PlayerStats.builder().playerId("P004").totalScore(5000).avgDeaths(6.0).avgKills(20.0).region("India").isSuspicious(false).build(); // Highest score

        List<PlayerStats> mockList = Arrays.asList(p1, p2, p4); // Only non-suspicious returned by repository
        when(playerStatsRepository.findByIsSuspiciousFalseOrderByTotalScoreDescAvgDeathsAsc()).thenReturn(mockList);

        List<PlayerStats> leaderboard = leaderboardService.getGlobalLeaderboard();

        assertEquals(3, leaderboard.size());
        assertEquals("P004", leaderboard.get(0).getPlayerId(), "P004 should be 1st because of highest score");
        assertEquals("P002", leaderboard.get(1).getPlayerId(), "P002 should be 2nd because of tie-break (lower deaths)");
        assertEquals("P001", leaderboard.get(2).getPlayerId(), "P001 should be 3rd");
    }

    @Test
    void testMatchmakingCompatibility() {
        PlayerStats candidate1 = PlayerStats.builder().playerId("P001").region("India").avgPing(40).skillRating(1800).isSuspicious(false).build();
        PlayerStats candidate2 = PlayerStats.builder().playerId("P002").region("India").avgPing(80).skillRating(1850).isSuspicious(false).build();
        PlayerStats candidate3 = PlayerStats.builder().playerId("P004").region("India").avgPing(50).skillRating(2200).isSuspicious(false).build(); // Out of skill range
        
        when(playerStatsRepository.findByRegionAndIsSuspiciousFalse("India"))
                .thenReturn(Arrays.asList(candidate1, candidate2, candidate3));

        // Search for matches for India, Ping 60, Skill 1800
        List<PlayerStats> matches = matchmakingService.findMatchesFromDb("India", 60, 1800);

        // candidate1 (ping diff 20, skill diff 0) -> MATCH
        // candidate2 (ping diff 20, skill diff 50) -> MATCH
        // candidate3 (ping diff 10, skill diff 400) -> NO MATCH
        assertEquals(2, matches.size());
        assertTrue(matches.contains(candidate1));
        assertTrue(matches.contains(candidate2));
        assertFalse(matches.contains(candidate3));
    }
}
