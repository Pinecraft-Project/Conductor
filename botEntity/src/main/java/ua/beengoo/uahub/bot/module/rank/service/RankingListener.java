package ua.beengoo.uahub.bot.module.rank.service;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.HubBot;

/** JDA listener that forwards events to {@link RankingService}. */
@Service
public class RankingListener extends ListenerAdapter {

  private final RankingService rankingService;

  public RankingListener(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    rankingService.onMessage(event);
  }

  @Override
  public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
    rankingService.updateVoiceState(event);
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    try {
      String guildId = HubBot.getConfig().targetGuildId;
      var guild = event.getJDA().getGuildById(guildId);
      rankingService.bootstrapVoicePoolForGuild(guild);
    } catch (Throwable ignored) {
    }
  }
}
