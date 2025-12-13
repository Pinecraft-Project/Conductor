package ua.beengoo.uahub.bot.module.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

/** Observer for {@link PlayerInstance} events. */
public interface PlayerInstanceListener {

    void onPlaylistLoaded(AudioPlaylist playlist, AudioPlayer player);

    void onSearchFailed(AudioPlayer player);

    void onLoadFailed(FriendlyException e, AudioPlayer player);

    void onTrackPlaying(AudioTrackMeta track, AudioPlayer player);

    void onTrackEnd(AudioTrackMeta track, AudioTrackEndReason endReason, AudioPlayer player);

    void onTrackSkipped(AudioTrackMeta track, AudioPlayer player);

    void onTrackQueueAdded(AudioTrackMeta track, AudioPlayer player);

    void onPlaylistQueueAdded(AudioPlaylistMeta playlist, AudioPlayer player);

    void onTrackLoaded(AudioTrack track, AudioPlayer player);

    void onPlayerModeChanged(PlayerMode modeBefore, PlayerMode modeAfter, AudioPlayer player);

    void onQueueFinished(AudioPlayer player);

    void onSearchLoaded(AudioPlaylist audioPlaylist, AudioPlayer player);

    void onHeavyLoadWarn(AudioTrackMeta track, AudioPlayer player);
}
