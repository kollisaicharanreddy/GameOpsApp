package com.gameops.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats {

    @Id
    private String playerId;

    private String region;
    private int totalMatches;
    private int totalScore;
    
    private double avgPing;
    private double avgKills;
    private double avgDeaths;
    
    private int skillRating;
    
    @Builder.Default
    private boolean isSuspicious = false;
    
    @Builder.Default
    private int seasonId = 1;
}
