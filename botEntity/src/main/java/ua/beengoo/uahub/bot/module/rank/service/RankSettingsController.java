package ua.beengoo.uahub.bot.module.rank.service;

import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.module.rank.model.RankSettings;
import ua.beengoo.uahub.bot.module.rank.model.RankSettingsRepo;

@Service
public class RankSettingsController {

  private final RankSettingsRepo rankSettingsRepo;

  public RankSettingsController(RankSettingsRepo rankSettingsRepo) {
    this.rankSettingsRepo = rankSettingsRepo;
  }

  /**
   * Returns rank settings for the given server, creating defaults if missing.
   *
   * @param serverId guild id
   * @return settings entity
   */
  public RankSettings getSettings(long serverId) {
    if (rankSettingsRepo.existsByServerId(serverId)) {
      return rankSettingsRepo.findByServerId(serverId).orElse(null);
    } else {
      RankSettings rs = new RankSettings();
      rs.setServerId(serverId);
      rankSettingsRepo.save(rs);
      return rs;
    }
  }
}
