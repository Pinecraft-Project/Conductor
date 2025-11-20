package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;

/** Rank multipliers and flags for a specific role. */
@Entity
@Data
@Table(name = "role_rank_stats")
public class RoleRankStats {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "role_id")
  private long roleId;

  @Column(name = "excluded")
  private boolean excluded;

  @Column(name = "multiplier")
  private double roleMultiplier;

  /** Adjusts the role multiplier. */
  public void addMultiplier(double multiplier) {
    setRoleMultiplier(getRoleMultiplier() + multiplier);
  }
}
