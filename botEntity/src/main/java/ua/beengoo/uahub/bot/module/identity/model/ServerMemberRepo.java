package ua.beengoo.uahub.bot.module.identity.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerMemberRepo extends JpaRepository<ServerMember, Long> {
  /**
   * Finds a member by Discord user id.
   *
   * @param discordId Discord user id
   * @return optional entity
   */
  Optional<ServerMember> findByDiscordId(long discordId);

  /**
   * Checks if a member exists by Discord user id.
   *
   * @param discordId Discord user id
   * @return true if a record exists
   */
  boolean existsByDiscordId(long discordId);
}
