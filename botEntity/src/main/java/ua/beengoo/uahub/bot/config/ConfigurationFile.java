package ua.beengoo.uahub.bot.config;

/**
 * Plain configuration DTO persisted as JSON on disk.
 *
 * <p>Values from this file are injected into Spring at startup instead of using application.yml.
 */
public class ConfigurationFile {
  public String token = "place your bot token here";
  public String targetGuildId = "970251105558745118";
  public String postgresqlURL = "put";
  public String postgresqlUser = "it";
  public String postgresqlPassword = "here";

  /** Language code for messages bundle, e.g. "en" or "uk". */
  public String language = "en";
}
