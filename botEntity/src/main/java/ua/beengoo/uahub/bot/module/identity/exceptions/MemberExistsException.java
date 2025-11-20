package ua.beengoo.uahub.bot.module.identity.exceptions;

/** Thrown when attempting to add a member that already exists. */
public class MemberExistsException extends RuntimeException {
  public MemberExistsException(String message) {
    super(message);
  }
}
