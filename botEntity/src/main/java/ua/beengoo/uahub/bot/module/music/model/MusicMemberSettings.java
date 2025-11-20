package ua.beengoo.uahub.bot.module.music.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;

/** Per-member music settings stored as JSON payload for flexibility. */
@Entity
@Table(name = "music_member_settings")
public class MusicMemberSettings {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "owner_id", unique = true)
  @Getter
  @Setter
  private ServerMember owner;

  @Column(name = "json", nullable = false, length = 4096)
  @Getter
  @Setter
  private String json;
}
