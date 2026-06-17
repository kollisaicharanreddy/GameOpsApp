package com.gameops.controller;

import com.gameops.model.PlayerStats;
import com.gameops.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@RestController
public class MatchmakingController {

    @Autowired
    private MatchmakingService matchmakingService;

    // DTO for matchmaking search / join request
    public static record MatchmakingDTO(
            String playerId,
            String region,
            Integer ping,
            Integer skill
    ) {}

    /**
     * Endpoint 1: Quick direct compatibility search.
     * Match players based on: Same Region -> Ping Difference < 50ms -> Skill Difference < 200.
     */
    @PostMapping("/matchmaking")
    public ResponseEntity<List<PlayerStats>> findMatches(@RequestBody MatchmakingDTO dto) {
        if (dto.region() == null || dto.ping() == null || dto.skill() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<PlayerStats> matches = matchmakingService.findMatchesFromDb(dto.region(), dto.ping(), dto.skill());
        return ResponseEntity.ok(matches);
    }

    /**
     * Endpoint 2: Join matchmaking queue.
     * If 2 compatible players are found in the queue, forms a match group.
     */
    @PostMapping("/matchmaking/join")
    public ResponseEntity<?> joinQueue(@RequestBody MatchmakingDTO dto) {
        if (dto.playerId() == null || dto.region() == null || dto.ping() == null || dto.skill() == null) {
            return ResponseEntity.badRequest().body("Missing required fields (playerId, region, ping, skill)");
        }

        MatchmakingService.MatchmakingRequest req = new MatchmakingService.MatchmakingRequest(
                dto.playerId(),
                dto.region(),
                dto.ping(),
                dto.skill(),
                System.currentTimeMillis()
        );

        Optional<MatchmakingService.MatchGroup> matchGroup = matchmakingService.joinQueueAndMatch(req);

        if (matchGroup.isPresent()) {
            return ResponseEntity.ok(matchGroup.get());
        }

        return ResponseEntity.accepted().body("{\"status\": \"Searching\", \"message\": \"Added to matchmaking queue. Waiting for compatible players...\"}");
    }

    /**
     * Endpoint 3: View current matchmaking queue.
     */
    @GetMapping("/matchmaking/queue")
    public ResponseEntity<List<MatchmakingService.MatchmakingRequest>> getQueue() {
        return ResponseEntity.ok(matchmakingService.getQueue());
    }

    /**
     * Endpoint 4: Clear queue.
     */
    @PostMapping("/matchmaking/clear")
    public ResponseEntity<String> clearQueue() {
        matchmakingService.clearQueue();
        return ResponseEntity.ok("Matchmaking queue cleared");
    }
}
