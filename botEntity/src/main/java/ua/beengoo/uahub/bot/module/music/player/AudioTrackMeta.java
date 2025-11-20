package ua.beengoo.uahub.bot.module.music.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Wrapper around a LavaPlayer {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} that also
 * stores the requesting member.
 */
public class AudioTrackMeta {
  @Getter private AudioTrack entity;
  @Getter private Member entityOwner;

  public AudioTrackMeta(AudioTrack entity, Member owner) {
    this.entity = entity;
    this.entityOwner = owner;
  }
}
