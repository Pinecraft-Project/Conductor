package ua.beengoo.uahub.bot.module.permissions.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;

@Repository
public interface PermissionGroupRepo extends JpaRepository<PermissionGroup, Long> {
  /** Find a group by unique name. */
  Optional<PermissionGroup> findByName(String name);

  /** Find all groups marked as default. */
  List<PermissionGroup> findByDefaultGroupTrue();
}
