package ua.beengoo.uahub.bot.module.music.service;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.music.model.MusicMemberSettings;
import ua.beengoo.uahub.bot.module.music.player.PlayerMode;
import ua.beengoo.uahub.bot.module.music.repository.MusicMemberSettingsRepo;

@Service
public class MusicSettingsService {
  private final MusicMemberSettingsRepo repo;
  private final ServerMemberController memberController;
  private final Gson gson = new Gson();

  public MusicSettingsService(
      MusicMemberSettingsRepo repo, ServerMemberController memberController) {
    this.repo = repo;
    this.memberController = memberController;
  }

  @Transactional(readOnly = true)
  public Optional<PlayerMode> getSavedRepeatMode(long discordId) {
    return repo.findByOwnerDiscordId(discordId)
        .flatMap(
            ms -> {
              try {
                Map<?, ?> map = gson.fromJson(ms.getJson(), Map.class);
                if (map == null) return Optional.empty();
                Object rm = map.get("repeatMode");
                if (rm == null) return Optional.empty();
                return Optional.of(PlayerMode.valueOf(String.valueOf(rm)));
              } catch (Exception e) {
                return Optional.empty();
              }
            });
  }

  @Transactional
  public void saveRepeatMode(long discordId, PlayerMode mode) {
    ServerMember owner = memberController.addMemberOrNothing(discordId);
    MusicMemberSettings ms =
        repo.findByOwnerDiscordId(discordId)
            .orElseGet(
                () -> {
                  MusicMemberSettings n = new MusicMemberSettings();
                  n.setOwner(owner);
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
    map.put("repeatMode", mode.name());
    ms.setJson(gson.toJson(map));
    repo.save(ms);
  }

  @Transactional(readOnly = true)
  public java.util.Set<String> getRequiredVoteActions(long discordId) {
    return repo.findByOwnerDiscordId(discordId)
        .map(
            ms -> {
              try {
                Map<?, ?> map = gson.fromJson(ms.getJson(), Map.class);
                if (map == null) return defaultVoteActions();
                Object v = map.get("voteRequiredActions");
                if (v instanceof java.util.List<?> list) {
                  java.util.Set<String> s = new java.util.HashSet<>();
                  for (Object o : list) if (o != null) s.add(String.valueOf(o));
                  return s;
                }
                return defaultVoteActions();
              } catch (Exception e) {
                return defaultVoteActions();
              }
            })
        .orElseGet(this::defaultVoteActions);
  }

  @Transactional
  public java.util.Set<String> setActionRequired(
      long discordId, String actionKey, boolean required) {
    ServerMember owner = memberController.addMemberOrNothing(discordId);
    MusicMemberSettings ms =
        repo.findByOwnerDiscordId(discordId)
            .orElseGet(
                () -> {
                  MusicMemberSettings n = new MusicMemberSettings();
                  n.setOwner(owner);
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
    java.util.Set<String> actions = new java.util.HashSet<>(getRequiredVoteActions(discordId));
    if (required) actions.add(actionKey);
    else actions.remove(actionKey);
    map.put("voteRequiredActions", actions);
    ms.setJson(gson.toJson(map));
    repo.save(ms);
    return actions;
  }

  private java.util.Set<String> defaultVoteActions() {
    return new java.util.HashSet<>(java.util.Arrays.asList("skip", "stop"));
  }

  @Transactional(readOnly = true)
  public Optional<Boolean> getBypassVoting(long discordId) {
    return repo.findByOwnerDiscordId(discordId)
        .flatMap(
            ms -> {
              try {
                Map<?, ?> map = gson.fromJson(ms.getJson(), Map.class);
                if (map == null) return Optional.empty();
                Object v = map.get("bypassVoting");
                if (v == null) return Optional.empty();
                return Optional.of(Boolean.parseBoolean(String.valueOf(v)));
              } catch (Exception e) {
                return Optional.empty();
              }
            });
  }

  @Transactional
  public void saveBypassVoting(long discordId, boolean bypass) {
    ServerMember owner = memberController.addMemberOrNothing(discordId);
    MusicMemberSettings ms =
        repo.findByOwnerDiscordId(discordId)
            .orElseGet(
                () -> {
                  MusicMemberSettings n = new MusicMemberSettings();
                  n.setOwner(owner);
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
    map.put("bypassVoting", bypass);
    ms.setJson(gson.toJson(map));
    repo.save(ms);
  }
}
