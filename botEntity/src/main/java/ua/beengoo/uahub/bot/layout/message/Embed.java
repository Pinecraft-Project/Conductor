package ua.beengoo.uahub.bot.layout.message;

import net.dv8tion.jda.api.EmbedBuilder;

/** Factory utilities for embeds with consistent color layouts. */
public class Embed {
  private static final EmbedBuilder INFO = new EmbedBuilder().setColor(EmbedColorLayouts.NORMAL);
  private static final EmbedBuilder WARN = new EmbedBuilder().setColor(EmbedColorLayouts.WARNING);
  private static final EmbedBuilder ERROR = new EmbedBuilder().setColor(EmbedColorLayouts.ERROR);

  /** Creates a new info-styled {@link EmbedBuilder}. */
  public static EmbedBuilder getInfo() {
    return new EmbedBuilder() {
      {
        copyFrom(INFO);
      }
    };
  }

  /** Creates a new warning-styled {@link EmbedBuilder}. */
  public static EmbedBuilder getWarn() {
    return new EmbedBuilder() {
      {
        copyFrom(WARN);
      }
    };
  }

  /** Creates a new error-styled {@link EmbedBuilder}. */
  public static EmbedBuilder getError() {
    return new EmbedBuilder() {
      {
        copyFrom(ERROR);
      }
    };
  }
}
