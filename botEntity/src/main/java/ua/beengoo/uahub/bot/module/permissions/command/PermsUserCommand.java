package ua.beengoo.uahub.bot.module.permissions.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.permissions.model.MemberPermissionNode;
import ua.beengoo.uahub.bot.module.permissions.model.MemberPermissions;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

@Interaction
public class PermsUserCommand {
  private final ServerMemberController memberController;
  private final PermissionGroupRepo groupRepo;
  private final PermissionService permissionService;

  public PermsUserCommand() {
    this.memberController = ContextHolder.getBean(ServerMemberController.class);
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

  @Command(value = "perms-user", desc = "Керування правами користувача")
  public void onUser(
      CommandEvent event,
      @Param(name = "action")
          @Choices({
            "Додати групу:add-group",
            "Прибрати групу:remove-group",
            "Додати ноду:add-node",
            "Видалити ноду:remove-node"
          })
          String action,
      @Param(name = "user", value = "Користувач") net.dv8tion.jda.api.entities.Member user,
      @Param(name = "param1", value = "Назва групи або нода", optional = true) String p1,
      @Param(name = "param2", value = "allow true|false", optional = true) String p2) {

    if (!isAdmin(event)) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.error.insufficient_rights.title"))
              .setDescription(Lang.get("perms.error.insufficient_rights.desc")));
      return;
    }
    if (user == null) {
      event.reply(Embed.getError().setTitle(Lang.get("perms.user.invalid_user_id.title")));
      return;
    }
    long uid = user.getIdLong();
    ServerMember sm = memberController.addMemberOrNothing(uid);
    MemberPermissions mp = sm.getMemberPermissions();
    switch (action) {
      case "add-group" -> {
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.user.group.missing.title"))
                  .setDescription(Lang.get("perms.user.group.missing.desc")));
          return;
        }
        PermissionGroup g = groupRepo.findByName(p1).orElse(null);
        if (g == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(p1));
          return;
        }
        mp.getGroups().add(g);
        memberController.updateMember(sm);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.user.group.added.title"))
                .setDescription(
                    Lang.get("perms.user.group.added.desc")
                        .formatted(p1, user.getEffectiveName())));
      }
      case "remove-group" -> {
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.user.group.missing.title"))
                  .setDescription(Lang.get("perms.user.group.missing.desc")));
          return;
        }
        boolean removed = mp.getGroups().removeIf(x -> x.getName().equalsIgnoreCase(p1));
        memberController.updateMember(sm);
        event.reply(
            (removed ? Embed.getInfo() : Embed.getWarn())
                .setTitle(Lang.get("perms.user.group.removed.title"))
                .setDescription(
                    Lang.get("perms.user.group.removed.desc")
                        .formatted(p1, user.getEffectiveName())));
      }
      case "add-node" -> {
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.user.node.missing.title"))
                  .setDescription(Lang.get("perms.user.node.missing.desc")));
          return;
        }
        boolean allow = p2 == null || Boolean.parseBoolean(p2);
        MemberPermissionNode node = new MemberPermissionNode();
        node.setMemberPermissions(mp);
        node.setNode(p1);
        node.setAllowed(allow);
        mp.getNodes().add(node);
        memberController.updateMember(sm);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.user.node.added.title"))
                .setDescription(
                    Lang.get("perms.user.node.added.desc")
                        .formatted(p1, user.getEffectiveName(), (allow ? "allow" : "deny"))));
      }
      case "remove-node" -> {
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.user.node.missing.title"))
                  .setDescription(Lang.get("perms.user.node.missing.desc")));
          return;
        }
        boolean removed = mp.getNodes().removeIf(x -> x.getNode().equalsIgnoreCase(p1));
        memberController.updateMember(sm);
        event.reply(
            (removed ? Embed.getInfo() : Embed.getWarn())
                .setTitle(Lang.get("perms.user.node.removed.title"))
                .setDescription(
                    Lang.get("perms.user.node.removed.desc")
                        .formatted(p1, user.getEffectiveName())));
      }
      default -> event.reply(
          Embed.getWarn()
              .setTitle(Lang.get("perms.group.unknown_action.title"))
              .setDescription(action));
    }
  }
}
