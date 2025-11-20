package ua.beengoo.uahub.bot.module.music.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.beengoo.uahub.bot.module.music.model.MusicGuildSettings;

public interface MusicGuildSettingsRepo extends JpaRepository<MusicGuildSettings, Long> {
  Optional<MusicGuildSettings> findByGuildId(String guildId);
}
