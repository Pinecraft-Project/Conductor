package ua.beengoo.uahub.bot.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring configuration for bootstrapping a JDA instance and wiring event listeners. */
@Configuration
@Slf4j
public class JdaConfiguration {

  @Value("${discord.bot.secret}")
  private String token;

  /**
   * Builds and starts a ready JDA instance and registers all Spring-managed {@link
   * ListenerAdapter}s.
   *
   * @param listeners event listeners discovered by Spring
   * @return ready JDA instance
   * @throws Exception if JDA fails to initialize
   */
  @Bean
  public JDA jda(List<ListenerAdapter> listeners) throws Exception {
    JDABuilder builder = JDABuilder.createDefault(token);

    for (ListenerAdapter listener : listeners) {
      log.info("Adding {} to JDA event listeners.", listener.getClass().getName());
      builder.addEventListeners(listener);
    }

    return builder.build().awaitReady();
  }
}
