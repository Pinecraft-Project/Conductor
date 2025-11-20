package ua.beengoo.uahub.bot.module.music.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A single track entry in a user playlist with a stable order position. */
@Entity
@Table(name = "music_user_playlist_tracks")
public class UserPlaylistTrack {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "playlist_id")
  @Getter
  @Setter
  private UserPlaylist playlist;

  @Column(name = "position", nullable = false)
  @Getter
  @Setter
  private int position;

  @Column(name = "query", nullable = false, length = 2048)
  @Getter
  @Setter
  private String query;

  @Column(name = "title", length = 512)
  @Getter
  @Setter
  private String title;
}
