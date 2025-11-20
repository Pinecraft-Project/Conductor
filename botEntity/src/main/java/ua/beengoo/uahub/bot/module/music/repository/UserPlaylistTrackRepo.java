package ua.beengoo.uahub.bot.module.music.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.beengoo.uahub.bot.module.music.model.UserPlaylistTrack;

public interface UserPlaylistTrackRepo extends JpaRepository<UserPlaylistTrack, Long> {}
