package ua.beengoo.uahub.bot.module.music.player;

/** Thrown when the provided URL or source type is not supported. */
public class UnsupportedSourceException extends PlayerException {
  public UnsupportedSourceException(String message) {
    super(message);
  }
}
