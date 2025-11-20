package ua.beengoo.uahub.bot.module.moderation.command;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;

@Interaction
/** Slash command: bulk delete or selective delete of recent messages in a channel. */
public class ClearCommand {
  private static final int MAX_CLEAR_LIMIT = 1000; // bigger limit than 100
  private static final Set<Long> CHANNEL_LOCKS = ConcurrentHashMap.newKeySet();
  private final PermissionService permissionService;

  public ClearCommand() {
    this.permissionService = ContextHolder.getBean(PermissionService.class);
  }

  private boolean allowed(CommandEvent event) {
    boolean hasDiscordManage =
        event.getMember() != null && event.getMember().hasPermission(Permission.MESSAGE_MANAGE);
    boolean hasNode =
        permissionService.has(
            event.getUser().getIdLong(),
            event.getMember() != null
                ? event.getMember().getRoles().stream().map(Role::getIdLong).toList()
                : null,
            "mod.clear");
    return hasDiscordManage || hasNode;
  }

  /**
   * Handler for the /clear command. Mirrors provided ClearControls behavior with: - larger amount
   * limit (up to MAX_CLEAR_LIMIT) - per-channel lock while a clear is running - ability to delete
   * up to a specific message id (tomid)
   */
  @Command(value = "clear", desc = "Очистити повідомлення у каналі")
  public void onClear(
      CommandEvent event,
      @Param(name = "amount", value = "Скільки повідомлень видалити (1-1000)", optional = true)
          Integer amount,
      @Param(
              name = "tomid",
              value = "Видаляє повідомлення до вказаного повідомлення",
              optional = true)
          String toMID,
      @Param(name = "user", value = "Лише цього користувача (опційно)", optional = true)
          net.dv8tion.jda.api.entities.Member user) {
    if (!allowed(event)) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.error.insufficient_rights.title"))
              .setDescription(Lang.get("perms.error.insufficient_rights.desc")));
      return;
    }
    MessageChannelUnion channel = (MessageChannelUnion) event.jdaEvent().getChannel();
    if (!channel.getType().isMessage()) {
      event.reply(Embed.getWarn().setTitle(Lang.get("mod.clear.not_message_channel")));
      return;
    }

    long channelId = channel.getIdLong();
    if (!CHANNEL_LOCKS.add(channelId)) {
      event.reply(
          Embed.getWarn()
              .setTitle(Lang.get("mod.clear.locked.title"))
              .setDescription(Lang.get("mod.clear.locked.desc")));
      return;
    }

    // Make replies ephemeral to avoid deleting our own status message
    event.jdaEvent().deferReply(true).queue();

    final long startMs = System.currentTimeMillis();
    try {
      if (amount != null) {
        if (amount < 0 || amount > MAX_CLEAR_LIMIT) {
          event
              .jdaEvent()
              .getHook()
              .sendMessageEmbeds(
                  Embed.getWarn()
                      .setTitle(Lang.get("mod.clear.invalid_amount.value.title").formatted(amount))
                      .setDescription(
                          Lang.get("mod.clear.invalid_amount.value.desc")
                              .formatted(MAX_CLEAR_LIMIT))
                      .build())
              .queue(v -> CHANNEL_LOCKS.remove(channelId));
          return;
        }
        deleteByAmount(
            channel, amount, new AtomicInteger(), new AtomicInteger(), event, channelId, startMs);
      } else if (toMID != null) {
        deleteToMessageId(channel, toMID, event, channelId, startMs);
      } else {
        event
            .jdaEvent()
            .getHook()
            .sendMessageEmbeds(
                Embed.getWarn().setTitle(Lang.get("mod.clear.invalid_amount")).build())
            .queue(v -> CHANNEL_LOCKS.remove(channelId));
      }
    } catch (Throwable t) {
      CHANNEL_LOCKS.remove(channelId);
      event
          .jdaEvent()
          .getHook()
          .sendMessageEmbeds(Embed.getError().setTitle(Lang.get("mod.clear.failed")).build())
          .queue();
    }
  }

  private static String plural(int count) {
    String form;
    if (count % 99 >= 11 && count % 100 <= 14) {
      form = "повідомлень";
    } else if (count % 9 == 1) {
      form = "повідомлення";
    } else if (count % 9 >= 2 && count % 10 <= 4) {
      form = "повідомлення";
    } else {
      form = "повідомлень";
    }
    return form;
  }

  private void deleteByAmount(
      MessageChannel channel,
      int remaining,
      AtomicInteger tries,
      AtomicInteger fails,
      CommandEvent event,
      long channelId,
      long startMs) {
    if (remaining <= 0) {
      int count = tries.get() - fails.get();
      var embed =
          Embed.getInfo()
              .setTitle(Lang.get("mod.clear.done"))
              .setDescription(
                  Lang.get("mod.clear.deleted").formatted(count)
                      + "\n"
                      + Lang.get("mod.clear.result.desc")
                          .formatted(formatDuration(System.currentTimeMillis() - startMs)))
              .build();

      event
          .jdaEvent()
          .getHook()
          .sendMessageEmbeds(embed)
          .queue(v -> CHANNEL_LOCKS.remove(channelId));
      return;
    }

    int batch = Math.min(100, remaining);
    channel
        .getHistory()
        .retrievePast(batch)
        .queue(
            messages -> {
              if (messages.isEmpty()) {
                int count = tries.get() - fails.get();
                var embed =
                    Embed.getInfo()
                        .setTitle(Lang.get("mod.clear.done"))
                        .setDescription(
                            Lang.get("mod.clear.deleted").formatted(count)
                                + "\n"
                                + Lang.get("mod.clear.result.desc")
                                    .formatted(
                                        formatDuration(System.currentTimeMillis() - startMs)))
                        .build();
                event
                    .jdaEvent()
                    .getHook()
                    .sendMessageEmbeds(embed)
                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                return;
              }

              AtomicInteger pending = new AtomicInteger(messages.size());
              AtomicInteger deletedNow = new AtomicInteger();
              for (Message m : messages) {
                try {
                  m.delete()
                      .queue(
                          s -> {
                            tries.incrementAndGet();
                            deletedNow.incrementAndGet();
                            if (pending.decrementAndGet() == 0) {
                              if (deletedNow.get() <= 0) {
                                int count = tries.get() - fails.get();
                                var embed =
                                    Embed.getInfo()
                                        .setTitle(Lang.get("mod.clear.done"))
                                        .setDescription(
                                            Lang.get("mod.clear.deleted").formatted(count)
                                                + "\n"
                                                + Lang.get("mod.clear.result.desc")
                                                    .formatted(
                                                        formatDuration(
                                                            System.currentTimeMillis() - startMs)))
                                        .build();
                                event
                                    .jdaEvent()
                                    .getHook()
                                    .sendMessageEmbeds(embed)
                                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                              } else {
                                deleteByAmount(
                                    channel,
                                    remaining - deletedNow.get(),
                                    tries,
                                    fails,
                                    event,
                                    channelId,
                                    startMs);
                              }
                            }
                          },
                          err -> {
                            tries.incrementAndGet();
                            fails.incrementAndGet();
                            if (pending.decrementAndGet() == 0) {
                              if (deletedNow.get() <= 0) {
                                int count = tries.get() - fails.get();
                                var embed =
                                    Embed.getInfo()
                                        .setTitle(Lang.get("mod.clear.done"))
                                        .setDescription(
                                            Lang.get("mod.clear.deleted").formatted(count)
                                                + "\n"
                                                + Lang.get("mod.clear.result.desc")
                                                    .formatted(
                                                        formatDuration(
                                                            System.currentTimeMillis() - startMs)))
                                        .build();
                                event
                                    .jdaEvent()
                                    .getHook()
                                    .sendMessageEmbeds(embed)
                                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                              } else {
                                deleteByAmount(
                                    channel,
                                    remaining - deletedNow.get(),
                                    tries,
                                    fails,
                                    event,
                                    channelId,
                                    startMs);
                              }
                            }
                          });
                } catch (Throwable ignored) {
                  fails.incrementAndGet();
                  if (pending.decrementAndGet() == 0) {
                    if (deletedNow.get() <= 0) {
                      int count = tries.get() - fails.get();
                      var embed =
                          Embed.getInfo()
                              .setTitle(Lang.get("mod.clear.done"))
                              .setDescription(
                                  Lang.get("mod.clear.deleted").formatted(count)
                                      + "\n"
                                      + Lang.get("mod.clear.result.desc")
                                          .formatted(
                                              formatDuration(System.currentTimeMillis() - startMs)))
                              .build();
                      event
                          .jdaEvent()
                          .getHook()
                          .sendMessageEmbeds(embed)
                          .queue(v -> CHANNEL_LOCKS.remove(channelId));
                    } else {
                      deleteByAmount(
                          channel,
                          remaining - deletedNow.get(),
                          tries,
                          fails,
                          event,
                          channelId,
                          startMs);
                    }
                  }
                }
              }
            },
            failure -> {
              event
                  .jdaEvent()
                  .getHook()
                  .sendMessageEmbeds(
                      Embed.getError().setTitle(Lang.get("mod.clear.failed")).build())
                  .queue(v -> CHANNEL_LOCKS.remove(channelId));
            });
  }

  private void deleteToMessageId(
      MessageChannel channel, String toMID, CommandEvent event, long channelId, long startMs) {
    channel
        .getHistory()
        .retrievePast(100)
        .queue(
            messages -> {
              int idx = -1;
              for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getId().equals(toMID)) {
                  idx = i;
                  break;
                }
              }

              if (idx == -1) {
                var embed =
                    Embed.getError()
                        .setTitle(Lang.get("mod.clear.tomessage.not_found.title"))
                        .setDescription(Lang.get("mod.clear.tomessage.not_found.desc"))
                        .build();
                event
                    .jdaEvent()
                    .getHook()
                    .sendMessageEmbeds(embed)
                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                return;
              }
              var sublist = messages.subList(0, idx + 1);
              AtomicInteger pending = new AtomicInteger(sublist.size());
              AtomicInteger tries = new AtomicInteger();
              AtomicInteger fails = new AtomicInteger();
              java.util.concurrent.atomic.AtomicBoolean finished =
                  new java.util.concurrent.atomic.AtomicBoolean(false);

              for (Message message : sublist) {
                try {
                  message
                      .delete()
                      .queue(
                          s -> {
                            tries.incrementAndGet();
                            if (message.getId().equals(toMID) || pending.decrementAndGet() == 0) {
                              if (finished.compareAndSet(false, true)) {
                                int count = tries.get() - fails.get();
                                var embed =
                                    Embed.getInfo()
                                        .setTitle(Lang.get("mod.clear.done"))
                                        .setDescription(
                                            Lang.get("mod.clear.deleted").formatted(count)
                                                + "\n"
                                                + Lang.get("mod.clear.result.desc")
                                                    .formatted(
                                                        formatDuration(
                                                            System.currentTimeMillis() - startMs)))
                                        .build();
                                event
                                    .jdaEvent()
                                    .getHook()
                                    .sendMessageEmbeds(embed)
                                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                              }
                            }
                          },
                          err -> {
                            fails.incrementAndGet();
                            if (message.getId().equals(toMID) || pending.decrementAndGet() == 0) {
                              if (finished.compareAndSet(false, true)) {
                                int count = tries.get() - fails.get();
                                var embed =
                                    Embed.getInfo()
                                        .setTitle("Видалено %d %s".formatted(count, plural(count)))
                                        .setDescription(
                                            "Повідомлення будуть видалені поступово, щоб уникнути"
                                                + " обмежень Discord.\n"
                                                + "Час виконання: "
                                                + formatDuration(
                                                    System.currentTimeMillis() - startMs))
                                        .build();
                                event
                                    .jdaEvent()
                                    .getHook()
                                    .sendMessageEmbeds(embed)
                                    .queue(v -> CHANNEL_LOCKS.remove(channelId));
                              }
                            }
                          });
                } catch (Throwable e) {
                  fails.incrementAndGet();
                }
              }
            },
            failure -> {
              event
                  .jdaEvent()
                  .getHook()
                  .sendMessageEmbeds(
                      Embed.getError().setTitle(Lang.get("mod.clear.failed")).build())
                  .queue(v -> CHANNEL_LOCKS.remove(channelId));
            });
  }

  private static String formatDuration(long ms) {
    if (ms < 1000) {
      return ms + " ms";
    }
    double sec = ms / 1000.0;
    if (sec < 60) {
      return String.format(java.util.Locale.ROOT, "%.2f s", sec);
    }
    long minutes = (long) (sec / 60);
    double remSec = sec - minutes * 60;
    return String.format(java.util.Locale.ROOT, "%d min %.1f s", minutes, remSec);
  }
}
