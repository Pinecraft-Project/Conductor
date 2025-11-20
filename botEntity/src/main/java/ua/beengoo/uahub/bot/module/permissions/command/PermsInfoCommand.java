package ua.beengoo.uahub.bot.module.permissions.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

@Interaction
public class PermsInfoCommand {
  private final PermissionGroupRepo groupRepo;
  private final PermissionService permissionService;

  public PermsInfoCommand() {
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

  @Command(value = "perms-groups", desc = "Перелік усіх груп")
  public void listGroups(CommandEvent event) {
    if (!isAdmin(event)) {
      event.reply(Embed.getError().setTitle(Lang.get("perms.error.insufficient_rights.title")));
      return;
    }
    String body =
        groupRepo.findAll().stream()
            .sorted(Comparator.comparingInt(PermissionGroup::getWeight))
            .map(
                g ->
                    (g.isDefaultGroup() ? "[def] " : "")
                        + g.getName()
                        + " (w="
                        + g.getWeight()
                        + ")")
            .collect(Collectors.joining("\n"));
    event.reply(
        Embed.getInfo()
            .setTitle(Lang.get("perms.groups.title"))
            .setDescription(body.isEmpty() ? Lang.get("perms.groups.empty") : body));
  }

  @Command(value = "perms-group-info", desc = "Інформація по групі")
  public void groupInfo(CommandEvent event, @Param(name = "group") String groupName) {
    if (!isAdmin(event)) {
      event.reply(Embed.getError().setTitle(Lang.get("perms.error.insufficient_rights.title")));
      return;
    }
    PermissionGroup g = groupRepo.findByName(groupName).orElse(null);
    if (g == null) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.group.not_found.title"))
              .setDescription(groupName));
      return;
    }
    String nodes =
        g.getPermissions().stream()
            .map(p -> (p.isAllowed() ? "+" : "-") + p.getNode())
            .sorted()
            .collect(Collectors.joining(", "));
    String parents =
        g.getParents().stream()
            .map(PermissionGroup::getName)
            .sorted()
            .collect(Collectors.joining(", "));
    event.reply(
        Embed.getInfo()
            .setTitle(Lang.get("perms.group.info.title").formatted(g.getName()))
            .setDescription(
                Lang.get("perms.group.info.desc")
                    .formatted(
                        g.getWeight(),
                        g.isDefaultGroup(),
                        (parents.isEmpty() ? "—" : parents),
                        (nodes.isEmpty() ? "—" : nodes))));
  }

  @Command(value = "perms-check", desc = "Перевірити дозвіл для користувача")
  public void check(
      CommandEvent event,
      @Param(name = "user", value = "Користувач") net.dv8tion.jda.api.entities.Member user,
      @Param(name = "node", value = "Нода для перевірки") String node,
      @Param(name = "role_ids", value = "ID ролей через кому", optional = true) String roleIds) {
    if (!isAdmin(event)) {
      event.reply(Embed.getError().setTitle(Lang.get("perms.error.insufficient_rights.title")));
      return;
    }
    if (user == null) {
      event.reply(Embed.getError().setTitle(Lang.get("perms.user.invalid_user_id.title")));
      return;
    }
    long uid = user.getIdLong();
    var roles =
        roleIds == null || roleIds.isBlank()
            ? null
            : java.util.Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    boolean allowed = permissionService.has(uid, roles, node);
    event.reply(
        (allowed ? Embed.getInfo() : Embed.getWarn())
            .setTitle(Lang.get("perms.check.title"))
            .setDescription(
                Lang.get("perms.check.desc")
                    .formatted(user.getEffectiveName(), node, (allowed ? "ALLOW" : "DENY"))));
  }
}
