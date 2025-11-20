package ua.beengoo.uahub.bot;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Utility for accessing localized messages from resource bundles. */
public class Lang {
  private static ResourceBundle bundle = loadBundle();

  private Lang() {}

  /**
   * Gets a localized string for the given key. If the key is missing in the bundle, the key itself
   * is returned.
   */
  public static String get(String key) {
    try {
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
  }

  /**
   * Gets a localized string or returns a default value if missing.
   *
   * @param key key to resolve
   * @param def default value if not found
   * @return resolved text or the default
   */
  public static String getOrDefault(String key, String def) {
    return get(key);
  }

  private static ResourceBundle loadBundle() {
    String lang = null;
    try {
      lang = HubBot.getConfig().language;
    } catch (Throwable ignored) {
    }
    if (lang == null || lang.isBlank()) lang = "en";
    Locale locale = Locale.forLanguageTag(lang);
    try {
      return ResourceBundle.getBundle("messages", locale);
    } catch (MissingResourceException e) {
      return ResourceBundle.getBundle("messages", Locale.ENGLISH);
    }
  }
}
