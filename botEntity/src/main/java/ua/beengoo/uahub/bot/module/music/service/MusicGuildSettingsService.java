package ua.beengoo.uahub.bot.module.music.service;

import com.google.gson.Gson;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.beengoo.uahub.bot.module.music.model.MusicGuildSettings;
import ua.beengoo.uahub.bot.module.music.repository.MusicGuildSettingsRepo;

@Service
public class MusicGuildSettingsService {
  private final MusicGuildSettingsRepo repo;
  private final Gson gson = new Gson();

  public MusicGuildSettingsService(MusicGuildSettingsRepo repo) {
    this.repo = repo;
  }

  /** Returns the set of action keys that require voting for this guild. */
  @Transactional(readOnly = true)
  public Set<String> getRequiredVoteActions(String guildId) {
    return repo.findByGuildId(guildId)
        .map(
            ms -> {
              try {
                Map<?, ?> json = gson.fromJson(ms.getJson(), Map.class);
                if (json == null) return defaultActions();
                Object v = json.get("voteRequiredActions");
                if (v instanceof List<?> list) {
                  Set<String> s = new HashSet<>();
                  for (Object o : list) if (o != null) s.add(String.valueOf(o));
                  return s;
                }
                return defaultActions();
              } catch (Exception e) {
                return defaultActions();
              }
            })
        .orElseGet(this::defaultActions);
  }

  /** Toggle action requirement. */
  @Transactional
  public Set<String> setActionRequired(String guildId, String actionKey, boolean required) {
    MusicGuildSettings ms =
        repo.findByGuildId(guildId)
            .orElseGet(
                () -> {
                  MusicGuildSettings n = new MusicGuildSettings();
                  n.setGuildId(guildId);
                  n.setJson("{}");
                  return n;
                });
    Map<String, Object> map;
    try {
      map = gson.fromJson(ms.getJson(), Map.class);
      if (map == null) map = new HashMap<>();
    } catch (Exception e) {
      map = new HashMap<>();
    }
    Set<String> actions = new HashSet<>(getRequiredVoteActions(guildId));
    if (required) actions.add(actionKey);
    else actions.remove(actionKey);
    map.put("voteRequiredActions", actions);
    ms.setJson(gson.toJson(map));
    repo.save(ms);
    return actions;
  }

  private Set<String> defaultActions() {
    // Sensible defaults: require votes for skip and stop only
    return new HashSet<>(Arrays.asList("skip", "stop"));
  }
}
