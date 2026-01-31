package ua.beengoo.uahub.bot.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;

public final class SearchManager {

    private static final AudioPlayerManager INSTANCE;

    static {
        INSTANCE = new DefaultAudioPlayerManager();
        INSTANCE.getConfiguration()
            .setResamplingQuality(AudioConfiguration.ResamplingQuality.LOW);

        YoutubeAudioSourceManager youtube =
            new YoutubeAudioSourceManager(
                true,
                new Web(),
                new AndroidMusic(),
                new Music()
            );
        INSTANCE.registerSourceManager(youtube);
        AudioSourceManagers.registerRemoteSources(INSTANCE);
    }

    public static AudioPlayerManager get() {
        return INSTANCE;
    }
}

