package ua.beengoo.uahub.bot.module.music.player;

import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

/** Helper for access control to player operations based on owner, roles and permission nodes. */
public class PlayerAccess {
  @Getter
  @Setter
  private static Long ownerId;

  public static void clearOwner() {
    ownerId = null;
  }

  public static boolean isOwner(long userId) {
    return ownerId != null && ownerId == userId;
  }

  /** Convenience overload using a command event. */
  public static boolean canControl(CommandEvent event) {
    return canControl(event.getUser().getIdLong(), event.getMember());
  }

  /** Returns whether the given user can interact with player controls. */
  public static boolean canControl(long userId, Member member) {
    if (isOwner(userId)) return true;
    PermissionService ps = ContextHolder.getBean(PermissionService.class);
    var roles = member != null ? member.getRoles().stream().map(Role::getIdLong).toList() : null;
    boolean hasNode =
        ps.has(userId, roles, "music.ctrl")
            || ps.has(userId, roles, "music.play")
            || ps.has(userId, roles, "music.pause")
            || ps.has(userId, roles, "music.skip")
            || ps.has(userId, roles, "music.prev")
            || ps.has(userId, roles, "music.next")
            || ps.has(userId, roles, "music.jump")
            || ps.has(userId, roles, "music.bye")
            || ps.has(userId, roles, "music.mode");
    return (member != null && member.hasPermission(Permission.ADMINISTRATOR)) || hasNode;
  }

  /** Checks a single permission node with Discord admin override. */
  public static boolean hasNode(long userId, Member member, String node) {
    // Discord admin override
    if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) return true;
    PermissionService ps = ContextHolder.getBean(PermissionService.class);
    var roles = member != null ? member.getRoles().stream().map(Role::getIdLong).toList() : null;
    return ps.has(userId, roles, node);
  }
}
