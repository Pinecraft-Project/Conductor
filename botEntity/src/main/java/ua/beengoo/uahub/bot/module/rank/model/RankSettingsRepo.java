package ua.beengoo.uahub.bot.module.rank.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankSettingsRepo extends JpaRepository<RankSettings, Long> {
  /** Finds settings for a server by id. */
  Optional<RankSettings> findByServerId(long serverId);

  /** Checks if settings exist for a server id. */
  boolean existsByServerId(long serverId);
}
