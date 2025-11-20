package ua.beengoo.uahub.bot.module.music.player;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Wrapper around a LavaPlayer {@link com.sedmelluq.discord.lavaplayer.track.AudioPlaylist} that can
 * produce {@link AudioTrackMeta} entries for each track.
 */
public class AudioPlaylistMeta {
  @Getter private AudioPlaylist entity;
  @Getter private Member entityOwner;

  public AudioPlaylistMeta(AudioPlaylist entity, Member owner) {
    this.entity = entity;
    this.entityOwner = owner;
  }

  /** Maps underlying playlist tracks to meta entries with the same owner. */
  public Collection<? extends AudioTrackMeta> getTracks() {
    List<AudioTrackMeta> trackMeta = new ArrayList<>();
    entity.getTracks().forEach(e -> trackMeta.add(new AudioTrackMeta(e, entityOwner)));
    return trackMeta;
  }
}
