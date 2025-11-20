package ua.beengoo.uahub.bot.module.music.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.beengoo.uahub.bot.module.music.model.MusicMemberSettings;

public interface MusicMemberSettingsRepo extends JpaRepository<MusicMemberSettings, Long> {
  Optional<MusicMemberSettings> findByOwnerDiscordId(long discordId);
}
