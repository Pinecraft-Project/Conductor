package ua.beengoo.uahub.bot.module.rank.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRankRepo extends JpaRepository<ChannelRankStats, Long> {
  /** Finds channel rank stats by channel id (or category id). */
  Optional<ChannelRankStats> findByChannelId(long channelId);

  /** Checks if channel rank stats exist by id. */
  boolean existsByChannelId(long channelId);
}
