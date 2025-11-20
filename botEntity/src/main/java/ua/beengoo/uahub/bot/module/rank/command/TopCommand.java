package ua.beengoo.uahub.bot.module.rank.command;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import java.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.helper.paginator.ButtonPaginator;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;
import ua.beengoo.uahub.bot.module.rank.model.RankStats;
import ua.beengoo.uahub.bot.module.rank.service.RankingService;

@Interaction
/** Slash command: shows top members by various ranking metrics with pagination. */
public class TopCommand {
  private final ServerMemberController serverMemberController;
  private final PermissionService permissionService;
  private final RankingService rankingService;

  public TopCommand() {
    serverMemberController = ContextHolder.getBean(ServerMemberController.class);
    permissionService = ContextHolder.getBean(PermissionService.class);
    rankingService = ContextHolder.getBean(RankingService.class);
  }

  /**
   * Handler for the /top command.
   *
   * @param topBy which metric to sort by (competitive, voice, chat, prime, level)
   */
  @Command(value = "top", desc = "Топ активних учасників серверу")
  public void onTopExec(
      CommandEvent event,
      @Param(name = "top_by", value = "Топ показнику")
          @Choices({
            "Змагальні очки:competitive",
            "Голосові очки:voice",
            "Чат очки:chat",
            "Prime очки:prime",
            "Ранг:level"
          })
          String topBy) {
    boolean allowed =
        permissionService.has(
            event.getUser().getIdLong(),
            event.getMember() != null
                ? event.getMember().getRoles().stream().map(Role::getIdLong).toList()
                : null,
            "rank.view");
    if (!allowed) {
      event.reply(
          Embed.getError()
              .setTitle(Lang.get("perms.error.insufficient_rights.title"))
              .setDescription(Lang.get("ranking.view.required")));
      return;
    }
    // Update voice pool stats on-demand for fresher data
    rankingService.updateVoiceRankState();

    List<MessageEmbed> pages = new ArrayList<>();
    List<RankStats> members = new ArrayList<>();

    serverMemberController
        .getAll()
        .forEach(
            serverMember -> {
              if (serverMember.getRankStats() != null) {
                members.add(serverMember.getRankStats());
              }
            });
    if (members.isEmpty()) {
      event.reply(
          Embed.getWarn()
              .setTitle(Lang.get("ranking.top.title"))
              .setDescription(Lang.get("ranking.top.empty")));
      return;
    }
    sortListBy(members, topBy);

    List<List<RankStats>> chunks = new ArrayList<>();
    for (int i = 0; i < members.size(); i += 10) {
      chunks.add(members.subList(i, Math.min(i + 10, members.size())));
    }
    int position = 0;
    int page = 0;
    for (List<RankStats> rankStats : chunks) {
      page++;
      EmbedBuilder eb = Embed.getInfo().setTitle(Lang.get("ranking.top.title"));
      eb.setFooter(Lang.get("ranking.top.footer").formatted(page, chunks.size()));
      StringBuilder sb = new StringBuilder();
      sb.append(Lang.get("ranking.top.description.%s".formatted(topBy)));
      for (RankStats stat : rankStats) {
        position++;
        sb.append(
            "\n **#%s** x%s %s: lvl%s; C %s (V: %s C: %s P: %s)"
                .formatted(
                    position,
                    stat.getMemberMultiplier(),
                    "<@" + stat.getServerMember().getDiscordId() + ">",
                    (int) stat.getLevel(),
                    stat.getCompetitivePoints(),
                    stat.getVoicePoints(),
                    stat.getChatPoints(),
                    stat.getPrimePoints()));
      }
      eb.setDescription(sb.toString());
      pages.add(eb.build());
    }

    event
        .jdaEvent()
        .replyEmbeds(pages.getFirst())
        .setEphemeral(false)
        .queue(
            s ->
                s.retrieveOriginal()
                    .queue(
                        sentMsg ->
                            new ButtonPaginator(pages, event.getUser(), false)
                                .paginate(sentMsg, event.getJDA())));
  }

  /** Sorts members by the provided metric in descending order. */
  public void sortListBy(List<RankStats> members, String sortBy) {
    switch (sortBy) {
      case "competitive" -> members.sort(
          Comparator.comparingDouble(RankStats::getCompetitivePoints).reversed());
      case "voice" -> members.sort(
          Comparator.comparingDouble(RankStats::getVoicePoints).reversed());
      case "chat" -> members.sort(Comparator.comparingDouble(RankStats::getChatPoints).reversed());
      case "prime" -> members.sort(
          Comparator.comparingDouble(RankStats::getPrimePoints).reversed());
      case "level" -> members.sort(Comparator.comparingDouble(RankStats::getLevel).reversed());
      default -> throw new IllegalArgumentException("Unknown sortBy param: %s".formatted(sortBy));
    }
  }
}
