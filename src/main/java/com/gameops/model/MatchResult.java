package com.gameops.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerId;
    private String matchId;
    private String region;
    private String device;

    private int ping;
    private int score;
    private int kills;
    private int deaths;

    private int matchDurationSeconds;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}