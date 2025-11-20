package ua.beengoo.uahub.bot.module.permissions.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "group_perms")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class GroupPermission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "group_id")
  private PermissionGroup group;

  // E.g., "rank.view" or "rank.*"
  @Column(nullable = false)
  @EqualsAndHashCode.Include
  @ToString.Include
  private String node;

  @Column(nullable = false)
  @ToString.Include
  private boolean allowed = true;
}
