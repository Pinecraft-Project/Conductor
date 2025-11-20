package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;

/** Rank multipliers and flags for a specific channel or its category. */
@Entity
@Data
@Table(name = "channel_rank_stats")
public class ChannelRankStats {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "channel_id")
  private long channelId;

  @Column(name = "excluded")
  private boolean excluded;

  @Column(name = "multiplier")
  private double channelMultiplier;

  /** Adjusts the channel multiplier. */
  public void addMultiplier(double multiplier) {
    setChannelMultiplier(getChannelMultiplier() + multiplier);
  }
}
