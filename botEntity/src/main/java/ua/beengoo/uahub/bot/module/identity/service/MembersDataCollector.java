package ua.beengoo.uahub.bot.module.identity.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.module.identity.exceptions.MemberExistsException;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;

@Slf4j
@Service
public class MembersDataCollector extends ListenerAdapter {
  /** Listens to messages and ensures authors are present in the database. */
  private final ServerMemberController serverMemberController;

  public MembersDataCollector(ServerMemberController serverMemberController) {
    this.serverMemberController = serverMemberController;
  }

  /**
   * On each non-bot message in guilds, attempts to create a {@link ServerMember} record for the
   * author if it doesn't exist.
   */
  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    Member member = event.getMember();
    if (member != null) {
      if (!member.getUser().isBot() && !member.getUser().isSystem()) {
        try {
          ServerMember serverMember =
              serverMemberController.addNewMember(event.getMember().getIdLong());
          log.info(
              "Member {} added to database with id {}",
              member.getEffectiveName(),
              serverMember.getId());
        } catch (MemberExistsException ignored) {
        }
      }
    }
  }
}
