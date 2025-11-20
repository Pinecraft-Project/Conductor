package ua.beengoo.uahub.bot.module.rank.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRankRepo extends JpaRepository<RoleRankStats, Long> {
  /** Finds role rank stats by role id. */
  Optional<RoleRankStats> findByRoleId(long roleId);

  /** Checks if role rank stats exist by id. */
  boolean existsByRoleId(long roleId);
}
