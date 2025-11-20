package ua.beengoo.uahub.bot.module.permissions.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Single permission node override attached to a member. */
@Entity
@Table(name = "member_perm_nodes")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class MemberPermissionNode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "member_perms_id")
  private MemberPermissions memberPermissions;

  @Column(nullable = false)
  @EqualsAndHashCode.Include
  @ToString.Include
  private String node;

  @Column(nullable = false)
  @ToString.Include
  private boolean allowed = true;
}
