package ua.beengoo.uahub.bot.module.permissions.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Mapping between a Discord role id and permission groups applied to members with that role. */
@Entity
@Table(name = "role_perm_mappings")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RolePermissionMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "role_id", nullable = false, unique = true)
  @EqualsAndHashCode.Include
  @ToString.Include
  private long roleId;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_perm_groups",
      joinColumns = @JoinColumn(name = "role_perm_mapping_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<PermissionGroup> groups = new HashSet<>();
}
