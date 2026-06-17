package com.gameops.repository;

import com.gameops.model.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, String> {
    List<PlayerStats> findByIsSuspiciousFalseOrderByTotalScoreDescAvgDeathsAsc();
    List<PlayerStats> findByRegionAndIsSuspiciousFalseOrderByTotalScoreDescAvgDeathsAsc(String region);
    List<PlayerStats> findByRegionAndIsSuspiciousFalse(String region);
}
