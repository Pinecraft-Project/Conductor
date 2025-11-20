package ua.beengoo.uahub.bot.module.permissions.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.model.ServerMemberRepo;
import ua.beengoo.uahub.bot.module.permissions.model.*;
import ua.beengoo.uahub.bot.module.permissions.repository.PermissionGroupRepo;
import ua.beengoo.uahub.bot.module.permissions.repository.RolePermissionMappingRepo;

@Service
public class PermissionService {

  private final ServerMemberRepo serverMemberRepo;
  private final PermissionGroupRepo groupRepo;
  private final RolePermissionMappingRepo roleMappingRepo;

  public PermissionService(
      ServerMemberRepo serverMemberRepo,
      PermissionGroupRepo groupRepo,
      RolePermissionMappingRepo roleMappingRepo) {
    this.serverMemberRepo = serverMemberRepo;
    this.groupRepo = groupRepo;
    this.roleMappingRepo = roleMappingRepo;
  }

  /**
   * Returns whether a user effectively has a permission node considering defaults, role groups and
   * user overrides.
   */
  @Transactional(readOnly = true)
  public boolean has(long discordUserId, Collection<Long> roleIds, String node) {
    ServerMember member = serverMemberRepo.findByDiscordId(discordUserId).orElse(null);
    if (member == null) {
      return resolve(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), node);
    }
    MemberPermissions mp = member.getMemberPermissions();
    Set<PermissionGroup> userGroups = mp != null ? mp.getGroups() : Collections.emptySet();
    Set<MemberPermissionNode> userNodes = mp != null ? mp.getNodes() : Collections.emptySet();
    Set<PermissionGroup> roleGroups = resolveRoleGroups(roleIds);
    return resolve(userGroups, roleGroups, userNodes, node);
  }

  /** Variant of {@link #has(long, Collection, String)} for an existing member entity. */
  @Transactional(readOnly = true)
  public boolean has(ServerMember member, Collection<Long> roleIds, String node) {
    MemberPermissions mp = member.getMemberPermissions();
    Set<PermissionGroup> userGroups = mp != null ? mp.getGroups() : Collections.emptySet();
    Set<MemberPermissionNode> userNodes = mp != null ? mp.getNodes() : Collections.emptySet();
    Set<PermissionGroup> roleGroups = resolveRoleGroups(roleIds);
    return resolve(userGroups, roleGroups, userNodes, node);
  }

  /** Lists all effective allowed nodes for a user. */
  @Transactional(readOnly = true)
  public Set<String> listEffectiveNodes(long discordUserId, Collection<Long> roleIds) {
    ServerMember member = serverMemberRepo.findByDiscordId(discordUserId).orElse(null);
    if (member == null) return Collections.emptySet();
    MemberPermissions mp = member.getMemberPermissions();
    Set<PermissionGroup> userGroups = mp != null ? mp.getGroups() : Collections.emptySet();
    Set<MemberPermissionNode> userNodes = mp != null ? mp.getNodes() : Collections.emptySet();
    Set<PermissionGroup> roleGroups = resolveRoleGroups(roleIds);

    LinkedHashMap<String, Boolean> resolved = resolveAll(userGroups, roleGroups, userNodes);
    return resolved.entrySet().stream()
        .filter(Map.Entry::getValue)
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Set<PermissionGroup> resolveRoleGroups(Collection<Long> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) return Collections.emptySet();
    Set<PermissionGroup> groups = new HashSet<>();
    for (Long roleId : roleIds) {
      roleMappingRepo.findByRoleId(roleId).ifPresent(mapping -> groups.addAll(mapping.getGroups()));
    }
    return groups;
  }

  private boolean resolve(
      Set<PermissionGroup> userGroups,
      Set<PermissionGroup> roleGroups,
      Set<MemberPermissionNode> userNodes,
      String target) {
    LinkedHashMap<String, Boolean> resolved = resolveAll(userGroups, roleGroups, userNodes);
    // Exact match first
    if (resolved.containsKey(target)) return resolved.get(target);
    // Wildcard match
    for (Map.Entry<String, Boolean> e : resolved.entrySet()) {
      String key = e.getKey();
      if (key.endsWith("*")) {
        String prefix = key.substring(0, key.length() - 1);
        if (target.startsWith(prefix)) return e.getValue();
      }
    }
    return false;
  }

  private LinkedHashMap<String, Boolean> resolveAll(
      Set<PermissionGroup> userGroups,
      Set<PermissionGroup> roleGroups,
      Set<MemberPermissionNode> userNodes) {
    // Start with defaults
    Set<PermissionGroup> allGroups = new HashSet<>(groupRepo.findByDefaultGroupTrue());
    allGroups.addAll(expandGroups(roleGroups));
    allGroups.addAll(expandGroups(userGroups));

    // Sort by weight ascending so later overrides with higher weight
    List<PermissionGroup> sorted = new ArrayList<>(allGroups);
    sorted.sort(Comparator.comparingInt(PermissionGroup::getWeight));

    LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
    for (PermissionGroup g : sorted) {
      for (GroupPermission gp : g.getPermissions()) {
        map.put(gp.getNode(), gp.isAllowed());
      }
    }
    // User-specific nodes override groups
    for (MemberPermissionNode node : userNodes) {
      map.put(node.getNode(), node.isAllowed());
    }
    return map;
  }

  private Set<PermissionGroup> expandGroups(Set<PermissionGroup> input) {
    Set<PermissionGroup> out = new HashSet<>();
    Deque<PermissionGroup> stack = new ArrayDeque<>(input);
    while (!stack.isEmpty()) {
      PermissionGroup g = stack.pop();
      if (out.add(g)) {
        for (PermissionGroup parent : g.getParents()) {
          stack.push(parent);
        }
      }
    }
    return out;
  }
}
