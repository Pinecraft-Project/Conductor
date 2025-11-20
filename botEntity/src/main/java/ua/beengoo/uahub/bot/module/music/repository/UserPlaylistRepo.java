package ua.beengoo.uahub.bot.module.music.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.beengoo.uahub.bot.module.music.model.UserPlaylist;

public interface UserPlaylistRepo extends JpaRepository<UserPlaylist, Long> {
  /** Find a playlist by owner and name (case-insensitive). */
  Optional<UserPlaylist> findByOwnerDiscordIdAndNameIgnoreCase(long ownerDiscordId, String name);

  /** List user playlists ordered by name ascending. */
  List<UserPlaylist> findByOwnerDiscordIdOrderByNameAsc(long ownerDiscordId);
}
