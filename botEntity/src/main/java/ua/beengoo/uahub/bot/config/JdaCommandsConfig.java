package ua.beengoo.uahub.bot.config;

import com.github.kaktushose.jda.commands.JDACommands;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Configuration;
import ua.beengoo.uahub.bot.HubBot;

/** Initializes JDA-Commands and scans the project for annotated interactions. */
@Configuration
public class JdaCommandsConfig {
  private final JDA jda;

  /**
   * Starts JDA-Commands using the provided JDA instance.
   *
   * @param jda ready JDA instance
   */
  public JdaCommandsConfig(JDA jda) {
    this.jda = jda;
    JDACommands.start(jda, HubBot.class, "ua.beengoo.uahub");
  }
}
