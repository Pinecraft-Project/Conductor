package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;

/** Server-level configuration for ranking behavior. */
@Entity
@Data
@Table(name = "rank_settings")
public class RankSettings {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "server_id")
  private long serverId;

  @Column(name = "chat_points_amount")
  private double chatPointsAmount = 1;
}
