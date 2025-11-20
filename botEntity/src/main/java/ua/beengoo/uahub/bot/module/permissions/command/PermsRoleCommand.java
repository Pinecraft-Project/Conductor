package ua.beengoo.uahub.bot.module.permissions.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import java.util.HashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;
import ua.beengoo.uahub.bot.module.permissions.model.RolePermissionMapping;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.repository.RolePermissionMappingRepo;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

@Interaction
public class PermsRoleCommand {
  private final RolePermissionMappingRepo roleRepo;
  private final PermissionGroupRepo groupRepo;
  private final PermissionService permissionService;

  public PermsRoleCommand() {
    this.roleRepo = ContextHolder.getBean(RolePermissionMappingRepo.class);
    this.groupRepo = ContextHolder.getBean(PermissionGroupRepo.class);
    this.permissionService = ContextHolder.getBean(PermissionService.class);
  }

  private boolean isAdmin(CommandEvent event) {
    boolean hasDiscordAdmin =
        event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR);
    boolean hasNode =
        permissionService.has(
            event.getUser().getIdLong(),
            event.getMember() != null
                ? event.getMember().getRoles().stream().map(Role::getIdLong).toList()
                : null,
            "perms.admin");
    return hasDiscordAdmin || hasNode;
  }

  @Command(value = "perms-role", desc = "Мапінг ролей Discord до груп")
  public void onRole(
      CommandEvent event,
      @Param(name = "action") @Choices({"Призначити:map", "Зняти:unmap"}) String action,
      @Param(name = "role", value = "Роль") net.dv8tion.jda.api.entities.Role role,
      @Param(name = "group", value = "Назва групи") String groupName) {
    if (!isAdmin(event)) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.error.insufficient_rights.title"))
              .setDescription(Lang.get("perms.error.insufficient_rights.desc")));
      return;
    }

    PermissionGroup group = groupRepo.findByName(groupName).orElse(null);
    if (group == null) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.group.not_found.title"))
              .setDescription(groupName));
      return;
    }

    if (role == null) {
      event.reply(Embed.getError().setTitle("Невірна роль"));
      return;
    }
    long rid = role.getIdLong();
    RolePermissionMapping mapping = roleRepo.findByRoleId(rid).orElse(null);
    switch (action) {
      case "map" -> {
        if (mapping == null) {
          mapping = new RolePermissionMapping();
          mapping.setRoleId(rid);
          mapping.setGroups(new HashSet<>());
        }
        mapping.getGroups().add(group);
        roleRepo.save(mapping);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.role.mapping.added.title"))
                .setDescription(
                    Lang.get("perms.role.mapping.added.desc")
                        .formatted(role.getName(), groupName)));
      }
      case "unmap" -> {
        if (mapping == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.role.mapping.missing.title"))
                  .setDescription(
                      Lang.get("perms.role.mapping.missing.desc").formatted(role.getName())));
          return;
        }
        boolean removed =
            mapping.getGroups().removeIf(g -> g.getName().equalsIgnoreCase(groupName));
        roleRepo.save(mapping);
        event.reply(
            (removed ? Embed.getInfo() : Embed.getWarn())
                .setTitle(Lang.get("perms.role.mapping.removed.title"))
                .setDescription(
                    Lang.get("perms.role.mapping.removed.desc")
                        .formatted(role.getName(), groupName)));
      }
      default -> event.reply(
          Embed.getWarn()
              .setTitle(Lang.get("perms.group.unknown_action.title"))
              .setDescription(action));
    }
  }
}
