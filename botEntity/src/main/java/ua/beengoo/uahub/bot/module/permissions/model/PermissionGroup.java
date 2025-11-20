package ua.beengoo.uahub.bot.module.permissions.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Group of permission nodes with weight and optional parents for inheritance. */
@Entity
@Table(name = "perm_groups")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class PermissionGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(unique = true, nullable = false)
  @EqualsAndHashCode.Include
  @ToString.Include
  private String name;

  // Higher weight overrides lower when resolving
  @Column(nullable = false)
  @ToString.Include
  private int weight = 0;

  @Column(name = "is_default", nullable = false)
  @ToString.Include
  private boolean defaultGroup = false;

  @OneToMany(
      mappedBy = "group",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<GroupPermission> permissions = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "perm_group_parents",
      joinColumns = @JoinColumn(name = "child_group_id"),
      inverseJoinColumns = @JoinColumn(name = "parent_group_id"))
  private Set<PermissionGroup> parents = new HashSet<>();
}
