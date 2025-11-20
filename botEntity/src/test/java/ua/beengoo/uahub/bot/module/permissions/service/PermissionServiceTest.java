package ua.beengoo.uahub.bot.module.permissions.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.model.ServerMemberRepo;
import ua.beengoo.uahub.bot.module.permissions.model.*;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.repository.RolePermissionMappingRepo;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

  @Mock private ServerMemberRepo memberRepo;
  @Mock private PermissionGroupRepo groupRepo;
  @Mock private RolePermissionMappingRepo roleMapRepo;

  @InjectMocks private PermissionService svc;

  private PermissionGroup group(String name, int weight, boolean def, GroupPermission... perms) {
    PermissionGroup g = new PermissionGroup();
    g.setName(name);
    g.setWeight(weight);
    g.setDefaultGroup(def);
    if (perms != null) {
      for (GroupPermission p : perms) {
        p.setGroup(g);
        g.getPermissions().add(p);
      }
    }
    return g;
  }

  private GroupPermission perm(String node, boolean allowed) {
    GroupPermission gp = new GroupPermission();
    gp.setNode(node);
    gp.setAllowed(allowed);
    return gp;
  }

  private MemberPermissionNode userNode(MemberPermissions mp, String node, boolean allowed) {
    MemberPermissionNode mn = new MemberPermissionNode();
    mn.setMemberPermissions(mp);
    mn.setNode(node);
    mn.setAllowed(allowed);
    return mn;
  }

  @Test
  @DisplayName("has: default groups applied when no member record exists")
  void has_DefaultGroups_NoMember() {
    when(memberRepo.findByDiscordId(1L)).thenReturn(Optional.empty());
    when(groupRepo.findByDefaultGroupTrue())
        .thenReturn(List.of(group("def", 0, true, perm("rank.view", true))));

    boolean result = svc.has(1L, List.of(), "rank.view");
    assertTrue(result);
  }

  @Test
  @DisplayName("has: wildcard group node matches target")
  void has_WildcardMatch() {
    when(groupRepo.findByDefaultGroupTrue())
        .thenReturn(List.of(group("def", 0, true, perm("mod.*", true))));

    ServerMember sm = new ServerMember();
    sm.setDiscordId(10L);
    when(memberRepo.findByDiscordId(10L)).thenReturn(Optional.of(sm));

    boolean ok = svc.has(10L, List.of(), "mod.clear");
    assertTrue(ok);
  }

  @Test
  @DisplayName("has: higher weight group overrides lower weight")
  void has_WeightOverride() {
    PermissionGroup low = group("low", 1, false, perm("rank.view", false));
    PermissionGroup high = group("high", 10, false, perm("rank.view", true));
    when(groupRepo.findByDefaultGroupTrue()).thenReturn(Collections.emptyList());

    // Map role -> both groups
    RolePermissionMapping rpm = new RolePermissionMapping();
    rpm.setRoleId(5L);
    rpm.getGroups().add(low);
    rpm.getGroups().add(high);
    when(roleMapRepo.findByRoleId(5L)).thenReturn(Optional.of(rpm));

    ServerMember sm = new ServerMember();
    sm.setDiscordId(2L);
    when(memberRepo.findByDiscordId(2L)).thenReturn(Optional.of(sm));

    boolean ok = svc.has(2L, List.of(5L), "rank.view");
    assertTrue(ok); // high weight allow should override low deny
  }

  @Test
  @DisplayName("has: user node overrides groups")
  void has_UserNodeOverrides() {
    PermissionGroup g = group("g", 0, true, perm("feature.use", true));
    when(groupRepo.findByDefaultGroupTrue()).thenReturn(List.of(g));

    MemberPermissions mp = new MemberPermissions();
    mp.getNodes().add(userNode(mp, "feature.use", false));
    ServerMember sm = new ServerMember();
    sm.setDiscordId(3L);
    sm.setMemberPermissions(mp);
    when(memberRepo.findByDiscordId(3L)).thenReturn(Optional.of(sm));

    boolean ok = svc.has(3L, List.of(), "feature.use");
    assertFalse(ok);
  }

  @Test
  @DisplayName("listEffectiveNodes: collects allowed nodes after resolution")
  void listEffectiveNodes() {
    PermissionGroup g1 =
        group("d", 0, true, perm("a.b", true), perm("c.*", true), perm("x", false));
    PermissionGroup g2 = group("r", 5, false, perm("x", true));
    when(groupRepo.findByDefaultGroupTrue()).thenReturn(List.of(g1));
    RolePermissionMapping rpm = new RolePermissionMapping();
    rpm.setRoleId(7L);
    rpm.getGroups().add(g2);
    when(roleMapRepo.findByRoleId(7L)).thenReturn(Optional.of(rpm));

    ServerMember sm = new ServerMember();
    sm.setDiscordId(4L);
    when(memberRepo.findByDiscordId(4L)).thenReturn(Optional.of(sm));

    Set<String> nodes = svc.listEffectiveNodes(4L, List.of(7L));
    assertTrue(nodes.contains("a.b"));
    assertTrue(nodes.contains("c.*"));
    assertTrue(nodes.contains("x"));
  }
}
