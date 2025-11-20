package ua.beengoo.uahub.bot.module.permissions.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import java.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.permissions.model.GroupPermission;
import ua.beengoo.uahub.bot.module.permissions.model.PermissionGroup;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

@Interaction
public class PermsGroupCommand {

  private final PermissionGroupRepo groupRepo;
  private final PermissionService permissionService;

  public PermsGroupCommand() {
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

  @Command(value = "perms-group", desc = "Керування групами дозволів")
  public void onGroup(
      CommandEvent event,
      @Param(name = "action")
          @Choices({
            "Створити:create",
            "За замовчуванням:set-default",
            "Додати ноду:add-node",
            "Видалити ноду:remove-node",
            "Додати батька:parent-add",
            "Прибрати батька:parent-remove",
            "Вага:weight",
            "Дерево:tree"
          })
          String action,
      @Param(name = "group", value = "Назва групи") String groupName,
      @Param(name = "param1", value = "Параметр 1 (node/parent/true|false/вага)", optional = true)
          String p1,
      @Param(name = "param2", value = "Параметр 2 (allow true|false)", optional = true) String p2) {

    if (!isAdmin(event)) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.error.insufficient_rights.title"))
              .setDescription(Lang.get("perms.error.insufficient_rights.desc")));
      return;
    }

    PermissionGroup group = groupRepo.findByName(groupName).orElse(null);
    switch (action) {
      case "create" -> {
        if (group != null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.exists.title"))
                  .setDescription(groupName));
          return;
        }
        PermissionGroup g = new PermissionGroup();
        g.setName(groupName);
        groupRepo.save(g);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.group.created.title"))
                .setDescription(groupName));
      }
      case "set-default" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        boolean def = Boolean.parseBoolean(p1);
        group.setDefaultGroup(def);
        groupRepo.save(group);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.group.set_default.title"))
                .setDescription(groupName + ": " + def));
      }
      case "add-node" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.node.missing.title"))
                  .setDescription(Lang.get("perms.group.node.missing.desc")));
          return;
        }
        boolean allow = p2 == null || Boolean.parseBoolean(p2);
        GroupPermission gp = new GroupPermission();
        gp.setGroup(group);
        gp.setNode(p1);
        gp.setAllowed(allow);
        group.getPermissions().add(gp);
        groupRepo.save(group);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.group.node.added.title"))
                .setDescription(
                    Lang.get("perms.group.node.added.desc")
                        .formatted(p1, groupName, (allow ? "allow" : "deny"))));
      }
      case "remove-node" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.node.missing.title"))
                  .setDescription(Lang.get("perms.group.node.missing.desc")));
          return;
        }
        boolean removed = group.getPermissions().removeIf(x -> x.getNode().equalsIgnoreCase(p1));
        groupRepo.save(group);
        event.reply(
            (removed ? Embed.getInfo() : Embed.getWarn())
                .setTitle(Lang.get("perms.group.node.removed.title"))
                .setDescription(
                    Lang.get("perms.group.node.removed.desc").formatted(p1, groupName)));
      }
      case "parent-add" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.parent.missing.title"))
                  .setDescription(Lang.get("perms.group.parent.missing.desc")));
          return;
        }
        PermissionGroup parent = groupRepo.findByName(p1).orElse(null);
        if (parent == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.parent.not_found.title"))
                  .setDescription(p1));
          return;
        }
        group.getParents().add(parent);
        groupRepo.save(group);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.group.parent.added.title"))
                .setDescription(
                    Lang.get("perms.group.parent.added.desc")
                        .formatted(parent.getName(), groupName)));
      }
      case "parent-remove" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.parent.missing.title"))
                  .setDescription(Lang.get("perms.group.parent.missing.desc")));
          return;
        }
        boolean removed = group.getParents().removeIf(x -> x.getName().equalsIgnoreCase(p1));
        groupRepo.save(group);
        event.reply(
            (removed ? Embed.getInfo() : Embed.getWarn())
                .setTitle(Lang.get("perms.group.parent.removed.title"))
                .setDescription(
                    Lang.get("perms.group.parent.removed.desc").formatted(p1, groupName)));
      }
      case "weight" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        if (p1 == null) {
          event.reply(
              Embed.getWarn()
                  .setTitle(Lang.get("perms.group.weight.missing.title"))
                  .setDescription(Lang.get("perms.group.weight.missing.desc")));
          return;
        }
        try {
          int w = Integer.parseInt(p1);
          group.setWeight(w);
          groupRepo.save(group);
          event.reply(
              Embed.getInfo()
                  .setTitle(Lang.get("perms.group.weight.updated.title"))
                  .setDescription(
                      Lang.get("perms.group.weight.updated.desc").formatted(groupName, w)));
        } catch (NumberFormatException e) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.weight.invalid.title"))
                  .setDescription(p1));
        }
      }
      case "tree" -> {
        if (group == null) {
          event.reply(
              Embed.getError()
                  .setTitle(Lang.get("perms.group.not_found.title"))
                  .setDescription(groupName));
          return;
        }
        String tree = buildParentTree(group);
        event.reply(
            Embed.getInfo()
                .setTitle(Lang.get("perms.group.tree.title").formatted(group.getName()))
                .setDescription(tree.isEmpty() ? "—" : tree));
      }
      default -> event.reply(
          Embed.getWarn()
              .setTitle(Lang.get("perms.group.unknown_action.title"))
              .setDescription(action));
    }
  }

  private String buildParentTree(PermissionGroup root) {
    StringBuilder sb = new StringBuilder();
    sb.append(root.getName()).append('\n');
    // Sort parents by name for stable output
    List<PermissionGroup> parents = new ArrayList<>(root.getParents());
    parents.sort(Comparator.comparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER));
    Set<Long> visited = new HashSet<>();
    if (root.getId() != null) visited.add(root.getId());
    for (int i = 0; i < parents.size(); i++) {
      PermissionGroup p = parents.get(i);
      boolean isLast = (i == parents.size() - 1);
      renderParentTree(p, sb, "", isLast, visited);
    }
    return sb.toString();
  }

  private void renderParentTree(
      PermissionGroup node, StringBuilder sb, String prefix, boolean isLast, Set<Long> visited) {
    String branch = isLast ? "└─ " : "├─ ";
    sb.append(prefix).append(branch).append(node.getName());
    Long id = node.getId();
    if (id != null && visited.contains(id)) {
      sb.append(" (cycle)");
      sb.append('\n');
      return;
    }
    sb.append('\n');
    if (id != null) visited.add(id);
    List<PermissionGroup> parents = new ArrayList<>(node.getParents());
    parents.sort(Comparator.comparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER));
    for (int i = 0; i < parents.size(); i++) {
      PermissionGroup p = parents.get(i);
      boolean last = (i == parents.size() - 1);
      String childPrefix = prefix + (isLast ? "   " : "│  ");
      renderParentTree(p, sb, childPrefix, last, visited);
    }
  }
}
