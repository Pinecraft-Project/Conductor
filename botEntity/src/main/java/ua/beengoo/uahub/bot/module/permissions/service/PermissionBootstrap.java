package ua.beengoo.uahub.bot.module.permissions.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.beengoo.uahub.bot.module.permissions.model.GroupPermission;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;

/** Ensures essential default permission groups and nodes exist on startup. */
@Component
public class PermissionBootstrap {
  private final PermissionGroupRepo groupRepo;

  public PermissionBootstrap(PermissionGroupRepo groupRepo) {
    this.groupRepo = groupRepo;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void ensureDefaults() {
    // Ensure a default group exists
    PermissionGroup def = groupRepo.findByDefaultGroupTrue().stream().findFirst().orElse(null);
    if (def == null) {
      def = new PermissionGroup();
      def.setName("default");
      def.setDefaultGroup(true);
      def.setWeight(0);
    }
    boolean hasRankView =
        def.getPermissions().stream()
            .anyMatch(p -> p.getNode().equals("rank.view") && p.isAllowed());
    if (!hasRankView) {
      GroupPermission gp = new GroupPermission();
      gp.setGroup(def);
      gp.setNode("rank.view");
      gp.setAllowed(true);
      def.getPermissions().add(gp);
    }
    groupRepo.save(def);
  }
}
