package ua.beengoo.uahub.bot.module.music.vote;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class VoteListener extends ListenerAdapter {
  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    String id = event.getComponentId();
    if (id == null) return;
    if (id.startsWith("vote:yes:")) {
      String voteId = id.substring("vote:yes:".length());
      VoteManager.addYes(voteId, event.getMember());
      event.deferEdit().queue();
    } else if (id.startsWith("vote:cancel:")) {
      String voteId = id.substring("vote:cancel:".length());
      VoteManager.cancel(voteId);
      event.deferEdit().queue();
    }
  }
}
