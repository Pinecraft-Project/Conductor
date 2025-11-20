package ua.beengoo.uahub.bot.module.music.vote;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ua.beengoo.uahub.bot.layout.message.Embed;

/** Simple in-memory vote manager for music actions. */
public class VoteManager {
  public record Vote(
      String id,
      long guildId,
      long textChannelId,
      long voiceChannelId,
      long initiatorId,
      String actionKey,
      Message message,
      Set<Long> voters,
      int required,
      Instant createdAt,
      Runnable onSuccess) {}

  private static final Map<String, Vote> votes = new ConcurrentHashMap<>();

  /**
   * Start a vote as the main reply to an interaction (slash, button, select, etc.). The vote is
   * rendered as the interaction response (non-ephemeral).
   */
  public static String startVote(
      IReplyCallback reply,
      Member initiator,
      AudioChannel voiceChannel,
      String actionKey,
      String title,
      String description,
      Runnable onSuccess) {
    String id = UUID.randomUUID().toString();
    int participants =
        (int) voiceChannel.getMembers().stream().filter(m -> !m.getUser().isBot()).count();
    int required = Math.max(1, (int) Math.ceil(participants * 0.5));
    MessageEmbed embed =
        Embed.getInfo()
            .setTitle(title)
            .setDescription(description + "\n\n" + progress(0, required))
            .build();
    List<ActionRow> rows =
        List.of(
            ActionRow.of(
                Button.success("vote:yes:" + id, "Vote Yes"),
                Button.danger("vote:cancel:" + id, "Cancel Vote")));

    reply
        .replyEmbeds(embed)
        .setComponents(rows)
        .setEphemeral(false)
        .queue(
            hook ->
                hook.retrieveOriginal()
                    .queue(
                        msg ->
                            votes.put(
                                id,
                                new Vote(
                                    id,
                                    initiator.getGuild().getIdLong(),
                                    msg.getChannel().getIdLong(),
                                    voiceChannel.getIdLong(),
                                    initiator.getIdLong(),
                                    actionKey,
                                    msg,
                                    new HashSet<>(),
                                    required,
                                    Instant.now(),
                                    onSuccess))));
    return id;
  }

  public static String startVote(
      JDA jda,
      Member initiator,
      AudioChannel voiceChannel,
      MessageChannel textChannel,
      String actionKey,
      String title,
      String description,
      Runnable onSuccess) {
    String id = UUID.randomUUID().toString();
    int participants =
        (int) voiceChannel.getMembers().stream().filter(m -> !m.getUser().isBot()).count();
    int required = Math.max(1, (int) Math.ceil(participants * 0.5));
    MessageEmbed embed =
        Embed.getInfo()
            .setTitle(title)
            .setDescription(description + "\n\n" + progress(0, required))
            .build();
    List<ActionRow> rows =
        List.of(
            ActionRow.of(
                Button.success("vote:yes:" + id, "Vote Yes"),
                Button.danger("vote:cancel:" + id, "Cancel Vote")));
    // Pick a channel if none provided: prefer a text channel in the same category as voice,
    // else system channel, else first text channel we can send to
    if (textChannel == null) {
      var guild = initiator.getGuild();
      var self = guild.getSelfMember();
      var parent = voiceChannel.getParentCategory();
      if (parent != null) {
        for (TextChannel tc : parent.getTextChannels()) {
          if (self == null
              || !self.hasPermission(tc, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
            continue;
          textChannel = tc;
          break;
        }
      }
      if (textChannel == null) {
        TextChannel sys = guild.getSystemChannel();
        if (sys != null
            && self != null
            && self.hasPermission(sys, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
          textChannel = sys;
        } else {
          for (TextChannel tc : guild.getTextChannels()) {
            if (self == null
                || !self.hasPermission(tc, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
              continue;
            textChannel = tc;
            break;
          }
        }
      }
    }
    if (textChannel != null) {
      MessageChannel finalChannel = textChannel;
      textChannel
          .sendMessageEmbeds(embed)
          .setComponents(rows)
          .queue(
              msg ->
                  votes.put(
                      id,
                      new Vote(
                          id,
                          initiator.getGuild().getIdLong(),
                          finalChannel.getIdLong(),
                          voiceChannel.getIdLong(),
                          initiator.getIdLong(),
                          actionKey,
                          msg,
                          new HashSet<>(),
                          required,
                          Instant.now(),
                          onSuccess)));
    }
    return id;
  }

  public static Optional<Vote> get(String id) {
    return Optional.ofNullable(votes.get(id));
  }

  public static void addYes(String id, Member voter) {
    Vote v = votes.get(id);
    if (v == null) return;
    if (voter == null || voter.getUser().isBot()) return;
    // Initiator cannot vote
    if (v.initiatorId() == voter.getIdLong()) return;
    // Must be in the same voice channel
    var vs = voter.getVoiceState();
    var ch = vs != null ? vs.getChannel() : null;
    if (ch == null || ch.getIdLong() != v.voiceChannelId()) return;
    v.voters().add(voter.getIdLong());
    int yes = v.voters().size();
    if (yes >= v.required()) {
      try {
        v.onSuccess().run();
      } catch (Throwable ignored) {
      }
      complete(v, true);
    } else {
      update(v, yes);
    }
  }

  public static void cancel(String id) {
    Vote v = votes.remove(id);
    if (v == null) return;
    try {
      v.message()
          .editMessageEmbeds(
              Embed.getWarn()
                  .setTitle("Vote cancelled")
                  .setDescription("Action: " + v.actionKey())
                  .build())
          .setComponents()
          .queue();
    } catch (Throwable ignored) {
    }
  }

  private static void update(Vote v, int yes) {
    try {
      v.message()
          .editMessageEmbeds(
              Embed.getInfo()
                  .setTitle("Voting in progress")
                  .setDescription("Action: " + v.actionKey() + "\n\n" + progress(yes, v.required()))
                  .build())
          .queue();
    } catch (Throwable ignored) {
    }
  }

  private static void complete(Vote v, boolean success) {
    votes.remove(v.id());
    try {
      v.message()
          .editMessageEmbeds(
              (success ? Embed.getInfo() : Embed.getWarn())
                  .setTitle(success ? "Vote passed" : "Vote failed")
                  .setDescription("Action: " + v.actionKey())
                  .build())
          .setComponents()
          .queue();
    } catch (Throwable ignored) {
    }
  }

  private static String progress(int yes, int required) {
    return "Yes: %d / %d".formatted(yes, required);
  }
}
