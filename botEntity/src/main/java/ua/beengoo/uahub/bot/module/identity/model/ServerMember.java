package ua.beengoo.uahub.bot.module.identity.model;

import jakarta.persistence.*;
import lombok.Data;
import ua.beengoo.uahub.bot.module.permissions.model.MemberPermissions;
import ua.beengoo.uahub.bot.module.rank.model.RankStats;

/** JPA entity representing a Discord guild member tracked by the bot. */
@Entity
@Data
@Table(name = "members")
public class ServerMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private long id;

  @Column(name = "discord_id")
  private long discordId;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "rank_stats_id")
  private RankStats rankStats;

  @OneToOne(mappedBy = "serverMember", cascade = CascadeType.ALL)
  private MemberPermissions memberPermissions;
}
