package ua.beengoo.uahub.bot.module.rank.service;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.HubBot;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.rank.data.VoiceMember;
import ua.beengoo.uahub.bot.module.rank.model.*;

/**
 * Core ranking logic: listens to message and voice events, awards points and maintains extended
 * voice statistics for members.
 */
@Service
@Slf4j
public class RankingService {
  private static final long MAX_VOICE_SESSION_POINTS_COMBO = 256;

  private final ServerMemberController serverMemberController;
  private final RankingStatsService rankingStatsService;

  private final RankSettings rankSettings;

  private final Map<Member, VoiceMember> voiceRankingPool =
      Collections.synchronizedMap(new HashMap<>());

  public RankingService(
      ServerMemberController serverMemberController,
      RankSettingsController rankSettingsController,
      RankingStatsService rankingStatsService) {
    this.serverMemberController = serverMemberController;

    this.rankSettings =
        rankSettingsController.getSettings(Long.parseLong(HubBot.getConfig().targetGuildId));
    this.rankingStatsService = rankingStatsService;
  }

  /** Processes a message and awards chat points when applicable. */
  public void onMessage(MessageReceivedEvent event) {
    if (event.isFromGuild() && !event.isWebhookMessage() && !event.getAuthor().isBot()) {
      StandardGuildChannel targetChannel = event.getGuildChannel().asStandardGuildChannel();
      Member targetMember = event.getMember();

      assert targetMember != null;

      switch (targetChannel.getType()) {
        case TEXT, STAGE, VOICE, FORUM -> {
          awardChatPoints(targetMember, targetChannel);
        }
      }
    }
  }

  /**
   * Periodically samples voice participants to award voice points and update extended voice-time
   * metrics.
   */
  @Scheduled(fixedDelay = 600000)
  public void updateVoiceRankState() {
    long currentTime = System.currentTimeMillis();
    voiceRankingPool.forEach(
        (member, voiceMember) -> {
          assert member.getVoiceState() != null;
          StandardGuildChannel channel = member.getVoiceState().getChannel();

          if (channel != null && validForVoicePool(member)) {
            long joinTime = voiceMember.getTimeConnected();
            double points = (double) ((currentTime - joinTime) / 60000);
            if (points >= MAX_VOICE_SESSION_POINTS_COMBO) {
              points = MAX_VOICE_SESSION_POINTS_COMBO;
            }
            awardVoicePoints(member, channel, points);
          }

          // Accumulate detailed voice stats (delta-based) for all members in pool
          long last = voiceMember.getLastSampleTime();
          if (last <= 0) last = currentTime;
          long delta = currentTime - last;
          if (delta > 0) {
            ServerMember sm = serverMemberController.addMemberOrNothing(member.getIdLong());
            RankStats stats = sm.getRankStats();
            int humanCount = 0;
            if (channel != null) {
              humanCount =
                  (int) channel.getMembers().stream().filter(m -> !m.getUser().isBot()).count();
            }
            boolean withOthers = humanCount >= 2;
            boolean muted =
                member.getVoiceState().isMuted() || member.getVoiceState().isSelfMuted();
            boolean deafened =
                member.getVoiceState().isDeafened() || member.getVoiceState().isSelfDeafened();

            if (withOthers) stats.addVoiceMsWithOthers(delta);
            else stats.addVoiceMsAlone(delta);
            if (muted) stats.addVoiceMsMuted(delta);
            if (deafened) stats.addVoiceMsDeafened(delta);
            if (withOthers && !muted && !deafened) stats.addVoiceMsActive(delta);

            serverMemberController.updateMember(sm);
          }
          voiceMember.setLastSampleTime(currentTime);
        });
  }

  /** Updates pool membership and stats on voice state changes. */
  public void updateVoiceState(GuildVoiceUpdateEvent event) {
    AudioChannelUnion joinedChannel = event.getChannelJoined();
    AudioChannelUnion leftChannel = event.getChannelLeft();

    if (joinedChannel != null) {
      // Always track members in pool for stats, regardless of mute/deafen state
      if (!event.getMember().getUser().isBot()) {
        voiceRankingPool.putIfAbsent(
            event.getMember(), new VoiceMember(System.currentTimeMillis()));
        // Count joins only if it's not a move (move => both joined & left not null)
        if (leftChannel == null) {
          ServerMember sm =
              serverMemberController.addMemberOrNothing(event.getMember().getIdLong());
          sm.getRankStats().incVoiceJoins();
          serverMemberController.updateMember(sm);
        }
      }
    }
    if (leftChannel != null) {
      VoiceMember vm = voiceRankingPool.get(event.getMember());
      if (vm != null) {
        long now = System.currentTimeMillis();
        long last = vm.getLastSampleTime();
        if (last <= 0) last = now;
        long delta = now - last;
        if (delta > 0) {
          ServerMember sm =
              serverMemberController.addMemberOrNothing(event.getMember().getIdLong());
          RankStats stats = sm.getRankStats();
          int humanCount = 0;
          humanCount =
              (int) leftChannel.getMembers().stream().filter(m -> !m.getUser().isBot()).count();
          boolean withOthers = humanCount >= 2;
          boolean muted =
              event.getMember().getVoiceState().isMuted()
                  || event.getMember().getVoiceState().isSelfMuted();
          boolean deafened =
              event.getMember().getVoiceState().isDeafened()
                  || event.getMember().getVoiceState().isSelfDeafened();

          if (withOthers) stats.addVoiceMsWithOthers(delta);
          else stats.addVoiceMsAlone(delta);
          if (muted) stats.addVoiceMsMuted(delta);
          if (deafened) stats.addVoiceMsDeafened(delta);
          if (withOthers && !muted && !deafened) stats.addVoiceMsActive(delta);

          serverMemberController.updateMember(sm);
        }
      }
      voiceRankingPool.remove(event.getMember());
    }
  }

  /** Bootstrap current voice participants on ready (members who were already in voice). */
  public void bootstrapVoicePoolForGuild(net.dv8tion.jda.api.entities.Guild guild) {
    if (guild == null) return;
    for (var vc : guild.getVoiceChannels()) {
      for (var m : vc.getMembers()) {
        if (!m.getUser().isBot()) {
          voiceRankingPool.putIfAbsent(m, new VoiceMember(System.currentTimeMillis()));
        }
      }
    }
    for (var sc : guild.getStageChannels()) {
      for (var m : sc.getMembers()) {
        if (!m.getUser().isBot()) {
          voiceRankingPool.putIfAbsent(m, new VoiceMember(System.currentTimeMillis()));
        }
      }
    }
  }

  /** Checks whether a member is eligible for voice pooling. */
  public boolean validForVoicePool(Member member) {
    if (member.getVoiceState() != null) {
      return !member.getVoiceState().isDeafened()
          && !member.getVoiceState().isMuted()
          && !member.getUser().isBot();
    } else return false;
  }

  /** Awards chat points and updates level for a member. */
  public void awardChatPoints(@NotNull Member member, @NotNull StandardGuildChannel channel) {
    ServerMember serverMember = serverMemberController.addMemberOrNothing(member.getIdLong());
    serverMember.getRankStats().incMessagesSent();
    serverMember
        .getRankStats()
        .addChatPoints(rankSettings.getChatPointsAmount() * getFinalMultiplier(member, channel));
    updateLevel(serverMember.getRankStats());
    serverMemberController.updateMember(serverMember);
    log.info("Final amount of chat points for {} {}",member.getEffectiveName() , rankSettings.getChatPointsAmount() * getFinalMultiplier(member, channel));

  }

  /** Returns the effective multiplier: best channel + best role + member multiplier. */
  public double getFinalMultiplier(@NotNull Member member, @NotNull StandardGuildChannel channel) {
    ServerMember serverMember = serverMemberController.addMemberOrNothing(member.getIdLong());
    return rankingStatsService.getBestChannelMultiplier(channel)
        + rankingStatsService.getBestRoleMultiplier(member)
        + serverMember.getRankStats().getMemberMultiplier();
  }

  /** Awards voice points and updates level for a member. */
  public void awardVoicePoints(
      @NotNull Member member, @NotNull StandardGuildChannel channel, double points) {
    ServerMember serverMember = serverMemberController.addMemberOrNothing(member.getIdLong());
    serverMember.getRankStats().addVoicePoints(points * getFinalMultiplier(member, channel));
    updateLevel(serverMember.getRankStats());
    serverMemberController.updateMember(serverMember);
    log.info("Final amount of voice points for {} {}",member.getEffectiveName(), points * getFinalMultiplier(member, channel));
  }

  /** Adds prime points to a member. */
  public void awardPrimePoints(ServerMember member, long amount) {
    member.getRankStats().addPrimePoints(amount);
    serverMemberController.updateMember(member);
  }

  /** Calculates points required to reach the next level. */
  public static long getPointsToNextLevel(RankStats stats) {
    double rawLevel = Math.sqrt((stats.getVoicePoints() + stats.getChatPoints()) / 150.0);
    double nextLevelPoints = 150 * Math.pow((int) (rawLevel) + 1, 2);
    return (long) (nextLevelPoints - (stats.getVoicePoints() + stats.getChatPoints()));
  }

  /** Calculates progress towards next level as percent [0..100]. */
  public static double getLevelProgress(RankStats stats) {
    double totalPoints = stats.getChatPoints() + stats.getVoicePoints();
    int level = (int) (stats.getLevel());
    double currentLevelPoints = 150 * Math.pow(level, 2);
    double nextLevelPoints = 150 * Math.pow(level + 1, 2);
    double progress = (totalPoints - currentLevelPoints) / (nextLevelPoints - currentLevelPoints);
    return Math.min(Math.max(progress * 100, 0), 100);
  }

  /** Recalculates member level from chat and voice points. */
  public void updateLevel(RankStats stats) {
    stats.setLevel(Math.sqrt(((stats.getVoicePoints() + stats.getChatPoints()) / 150)));
  }
}
