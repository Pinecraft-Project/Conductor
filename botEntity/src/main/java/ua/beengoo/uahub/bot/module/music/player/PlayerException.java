package ua.beengoo.uahub.bot.module.music.player;

/** Generic runtime exception for player-related errors. */
public class PlayerException extends RuntimeException {
  public PlayerException(String message) {
    super(message);
  }
}
