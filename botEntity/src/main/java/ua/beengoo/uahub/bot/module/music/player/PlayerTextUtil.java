package ua.beengoo.uahub.bot.module.music.player;

import ua.beengoo.uahub.bot.Lang;

/** Text helpers for music player UI. */
public class PlayerTextUtil {
  public static String friendlyMode(PlayerMode mode) {
    return switch (mode) {
      case REPEAT_ONE -> Lang.get("music.mode.single");
      case REPEAT_QUEUE -> Lang.get("music.mode.queue");
      case NOTHING -> Lang.get("music.mode.none");
    };
  }
}
