package ua.beengoo.uahub.bot.module.rank.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.beengoo.uahub.bot.module.rank.model.RankStats;

class RankingServiceTest {

  @Test
  @DisplayName("getPointsToNextLevel: computes expected gap")
  void pointsToNextLevel() {
    RankStats stats = new RankStats();
    stats.addChatPoints(0);
    stats.addVoicePoints(0);
    long gap0 = RankingService.getPointsToNextLevel(stats);
    // from level 0 to 1 -> 150*1^2 - 0 = 150
    assertEquals(150, gap0);

    stats.addChatPoints(150);
    long gap1 = RankingService.getPointsToNextLevel(stats);
    // from level 1 to 2 -> 150*2^2 - 150 = 450
    assertEquals(450, gap1);
  }
}
