package com.gameops.service;

import com.gameops.model.PlayerStats;
import com.gameops.repository.PlayerStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MatchmakingService {

    @Autowired
    private PlayerStatsRepository playerStatsRepository;

    // In-memory queue representing active players looking for a match
    private final List<MatchmakingRequest> matchmakingQueue = new CopyOnWriteArrayList<>();

    public static record MatchmakingRequest(
            String playerId,
            String region,
            int ping,
            int skill,
            long timestamp
    ) {}

    public static record MatchGroup(
            String matchId,
            List<PlayerStats> players,
            String region,
            double avgSkill,
            double avgPing
    ) {}

    /**
     * Direct database lookup: Find players in the DB who are close in ping and skill.
     */
    public List<PlayerStats> findMatchesFromDb(String region, int ping, int skill) {
        return playerStatsRepository.findByRegionAndIsSuspiciousFalse(region)
                .stream()
                .filter(p -> Math.abs(p.getAvgPing() - ping) < 50)
                .filter(p -> Math.abs(p.getSkillRating() - skill) < 200)
                .toList();
    }

    /**
     * Register a player in the matchmaking queue and try to find a match group.
     */
    public Optional<MatchGroup> joinQueueAndMatch(MatchmakingRequest request) {
        // Remove existing queue request for this player if any
        matchmakingQueue.removeIf(req -> req.playerId().equals(request.playerId()));
        
        // Add to queue
        matchmakingQueue.add(request);

        // Try to form a match group of size 2 (or more) for this player
        return tryToMatch(request.playerId());
    }

    public List<MatchmakingRequest> getQueue() {
        return new ArrayList<>(matchmakingQueue);
    }

    public void clearQueue() {
        matchmakingQueue.clear();
    }

    private synchronized Optional<MatchGroup> tryToMatch(String playerId) {
        Optional<MatchmakingRequest> playerReqOpt = matchmakingQueue.stream()
                .filter(req -> req.playerId().equals(playerId))
                .findFirst();

        if (playerReqOpt.isEmpty()) {
            return Optional.empty();
        }

        MatchmakingRequest playerReq = playerReqOpt.get();
        List<MatchmakingRequest> compatibleRequests = new ArrayList<>();
        compatibleRequests.add(playerReq);

        for (MatchmakingRequest req : matchmakingQueue) {
            if (req.playerId().equals(playerId)) continue;

            // Criteria: Same region, Ping delta < 50, Skill delta < 200
            if (req.region().equalsIgnoreCase(playerReq.region()) &&
                Math.abs(req.ping() - playerReq.ping()) < 50 &&
                Math.abs(req.skill() - playerReq.skill()) < 200) {
                compatibleRequests.add(req);
            }
        }

        // Suppose a match group requires at least 2 compatible players
        if (compatibleRequests.size() >= 2) {
            // Take the first 2 players to form a match (could be scaled to 4 or 5 depending on match requirements)
            List<MatchmakingRequest> groupRequests = compatibleRequests.subList(0, 2);
            List<PlayerStats> groupPlayers = new ArrayList<>();

            for (MatchmakingRequest gr : groupRequests) {
                // Remove matched players from queue
                matchmakingQueue.remove(gr);

                // Load stats for response (or create temporary/mock stats)
                PlayerStats stats = playerStatsRepository.findById(gr.playerId())
                        .orElse(PlayerStats.builder()
                                .playerId(gr.playerId())
                                .region(gr.region())
                                .skillRating(gr.skill())
                                .avgPing(gr.ping())
                                .totalMatches(0)
                                .build());
                groupPlayers.add(stats);
            }

            double avgSkill = groupPlayers.stream().mapToInt(PlayerStats::getSkillRating).average().orElse(0.0);
            double avgPing = groupPlayers.stream().mapToDouble(PlayerStats::getAvgPing).average().orElse(0.0);
            String matchId = "MATCH-" + System.currentTimeMillis();

            return Optional.of(new MatchGroup(matchId, groupPlayers, playerReq.region(), avgSkill, avgPing));
        }

        return Optional.empty();
    }
}
