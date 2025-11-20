package ua.beengoo.uahub.bot.module.music.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Guild-level music settings persisted as JSON for flexibility. */
@Entity
@Table(name = "music_guild_settings")
public class MusicGuildSettings {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private Long id;

  @Column(name = "guild_id", unique = true, nullable = false, length = 32)
  @Getter
  @Setter
  private String guildId;

  @Column(name = "json", nullable = false, length = 8192)
  @Getter
  @Setter
  private String json;
}
