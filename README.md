# Game Operations System (GameOps)

A robust backend prototype for managing high-concurrency multiplayer game operations. This system ingests match results, evaluates metrics for suspicious behavior, tracks real-time global and regional player leaderboards with custom tie-breaking rules, and matches queued players based on region, latency, and skill levels.

For design details and scalability blueprints, refer to [game-ops-design-doc.md](file:///c:/TRAINING/GameOpsApp/game-ops-design-doc.md).

---

## 🚀 Key Features

### 📥 Match Ingestion & Anti-Cheat
* **High-Throughput Ingestion**: Receives raw match metrics via `POST /submit-score`.
* **Suspicion Detection Engine**: Run rule-based scoring checks evaluating impossible scores, combat efficiency anomalies, high pings, and historical score outliers.
* **Rank Isolation**: Automatically strips suspicious players from public leaderboards.

### 🏆 Leaderboards & Tie-Breaking
* **Real-time Ranking**: Aggregated scoring statistics are automatically compiled.
* **Deterministic Tie-Breaking**:
  1. Higher total score
  2. Fewer average deaths
  3. Higher KD Ratio
  4. Alphabetical fallback on player ID
* **Regional Filters**: Fetch global or location-specific rankings.

### 🎮 Latency-Aware Skill Matchmaking
* **Queue System**: Thread-safe active matchmaking queue (`POST /matchmaking/join`).
* **Multi-Factor Compatibility Check**: Matches players of the same region under tight constraints:
  * Latency difference $< 50\text{ ms}$
  * Skill Rating difference $< 200$
* **Standardized Skill Calculation**: Multi-factor formula weighting performance metrics and total matches.

---

## 🛠️ Project Structure

The project is structured as a standard Spring Boot application:

```
c:\TRAINING\GameOpsApp
├── game-ops-design-doc.md                  # System Design Document
├── pom.xml                                 # Maven dependencies and configuration
└── src
    ├── main
    │   ├── java/com/gameops
    │   │   ├── GameOpsApplication.java     # Bootstraps the application
    │   │   ├── controller
    │   │   │   ├── MatchController.java    # Submit score endpoint
    │   │   │   ├── LeaderboardController.java # Global/Regional leaderboard metrics
    │   │   │   ├── MatchmakingController.java # Queue and match search endpoints
    │   │   │   ├── SuspiciousController.java # Fetch/flag suspicious players
    │   │   │   └── SystemController.java   # Internal database status details
    │   │   ├── service
    │   │   │   ├── MatchService.java       # Process submissions and updates
    │   │   │   ├── LeaderboardService.java # Sorts and tracks user rankings
    │   │   │   ├── MatchmakingService.java # Latency-aware matchmaking queue logic
    │   │   │   └── SuspiciousDetectionService.java # Anti-cheat scoring logic
    │   │   ├── repository
    │   │   │   ├── MatchResultRepository.java
    │   │   │   └── PlayerStatsRepository.java
    │   │   └── model
    │   │       ├── MatchResult.java        # DB entity representing a match
    │   │       └── PlayerStats.java        # Aggregated stats of a player
    │   └── resources
    │       ├── application.properties      # Central application configuration
    │       ├── application-neon.properties # Configuration for NeonDB Cloud Postgres
    │       └── static
    │           └── index.html              # Beautiful single-page control panel UI
    └── test
        └── java/com/gameops
            └── GameOpsServiceTest.java     # Complete JUnit test suite
```

### 🔗 Code Navigation

* **Controllers**: [MatchController.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/controller/MatchController.java) | [LeaderboardController.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/controller/LeaderboardController.java) | [MatchmakingController.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/controller/MatchmakingController.java) | [SuspiciousController.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/controller/SuspiciousController.java) | [SystemController.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/controller/SystemController.java)
* **Services**: [MatchService.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/service/MatchService.java) | [LeaderboardService.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/service/LeaderboardService.java) | [MatchmakingService.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/service/MatchmakingService.java) | [SuspiciousDetectionService.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/service/SuspiciousDetectionService.java)
* **Repositories**: [MatchResultRepository.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/repository/MatchResultRepository.java) | [PlayerStatsRepository.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/repository/PlayerStatsRepository.java)
* **Models**: [MatchResult.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/model/MatchResult.java) | [PlayerStats.java](file:///c:/TRAINING/GameOpsApp/src/main/java/com/gameops/model/PlayerStats.java)
* **Build System**: [pom.xml](file:///c:/TRAINING/GameOpsApp/pom.xml)

---

## ⚡ Getting Started

### Prerequisites
* **Java**: JDK 21
* **Maven**: Version 3.8+ or wrapper
* **Database**: Neon DB (Cloud PostgreSQL) configured via [application-neon.properties](file:///c:/TRAINING/GameOpsApp/src/main/resources/application-neon.properties)

### Run the Automated Tests
Ensure that the basic logic, suspicion calculations, and matchmaking rules are operating correctly:
```bash
mvn clean test
```

### Run the Application
Start the Spring Boot server:
```bash
mvn spring-boot:run
```
By default, the server spins up at: `http://localhost:8080`

---

## 🖥️ Management Dashboard UI

The application serves an interactive HTML-based dashboard directly at the root endpoint (`/`). 

You can use it to:
* Submit score presets (including normal scores and anomalous profiles designed to trigger the suspicious engine rules).
* Search matching players or register them into the matchmaking queue.
* Monitor global and regional leaderboards in real time.
* Inspect database connection metadata and link directly to your Neon DB console.
* Reset leaderboard seasons.
