package ua.beengoo.uahub.bot.module.permissions.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;

/** Per-member container for assigned groups and ad-hoc permission nodes. */
@Entity
@Data
@Table(name = "member_perms")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class MemberPermissions {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  @ToString.Include
  private long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "members_id", nullable = false, unique = true)
  private ServerMember serverMember;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "member_perm_groups",
      joinColumns = @JoinColumn(name = "member_perms_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<PermissionGroup> groups = new HashSet<>();

  @OneToMany(
      mappedBy = "memberPermissions",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<MemberPermissionNode> nodes = new HashSet<>();
}
