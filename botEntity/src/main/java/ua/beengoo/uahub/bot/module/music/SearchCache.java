package ua.beengoo.uahub.bot.module.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchCache {

    private static final int MAX_RESULTS = 10;
    private static final long TTL_MS = 60_000;

    private static final Map<Long, CacheEntry> CACHE = new ConcurrentHashMap<>();

    public static void put(long userId, List<AudioTrack> tracks) {
        CACHE.put(userId, new CacheEntry(
            System.currentTimeMillis(),
            tracks.stream().limit(MAX_RESULTS).toList()
        ));
    }

    public static AudioTrack get(long userId, int index) {
        CacheEntry entry = CACHE.get(userId);
        if (entry == null) return null;
        if (System.currentTimeMillis() - entry.created > TTL_MS) {
            CACHE.remove(userId);
            return null;
        }
        if (index < 0 || index >= entry.tracks.size()) return null;
        return entry.tracks.get(index);
    }

    public static List<AudioTrack> list(long userId) {
        CacheEntry entry = CACHE.get(userId);
        if (entry == null) return List.of();
        return entry.tracks;
    }

    private record CacheEntry(long created, List<AudioTrack> tracks) {}
}

