package ua.beengoo.uahub.bot.module.permissions.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.beengoo.uahub.bot.module.permissions.model.RolePermissionMapping;

@Repository
public interface RolePermissionMappingRepo extends JpaRepository<RolePermissionMapping, Long> {
  /** Find mapping for a Discord role id. */
  Optional<RolePermissionMapping> findByRoleId(long roleId);
}
