package ua.beengoo.uahub.bot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/** Controller for loading and saving the {@link ConfigurationFile} to a JSON file. */
public class ConfigurationFileCtrl {
  private final File file;
  private final ConfigurationFile config;
  private final Gson gson;

  /**
   * Creates a controller bound to a given file path.
   *
   * @param filePath path to the JSON configuration file
   */
  public ConfigurationFileCtrl(String filePath) {
    this.file = new File(filePath);
    this.config = new ConfigurationFile();
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  /**
   * Loads configuration from disk or creates a default file if missing.
   *
   * @return loaded configuration
   * @throws RuntimeException if loading fails
   */
  public ConfigurationFile loadOrCreateDefault() {
    if (!file.exists()) {
      save(config);
      return config;
    }
    try (Reader reader = new FileReader(file)) {
      return gson.fromJson(reader, ConfigurationFile.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load config", e);
    }
  }

  /**
   * Persists the provided configuration to disk.
   *
   * @param config configuration to save
   * @throws RuntimeException if saving fails
   */
  public void save(ConfigurationFile config) {
    try (Writer writer = new FileWriter(file)) {
      gson.toJson(config, writer);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save config", e);
    }
  }
}
