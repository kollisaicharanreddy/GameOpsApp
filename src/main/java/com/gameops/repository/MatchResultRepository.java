package com.gameops.repository;

import com.gameops.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByPlayerId(String playerId);
    List<MatchResult> findByRegion(String region);
}
