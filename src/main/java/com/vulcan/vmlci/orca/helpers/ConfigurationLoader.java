/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.helpers;

import com.cedarsoftware.util.io.JsonReader;
import com.opencsv.CSVReader;
import ij.Prefs;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

/** Helpers for loading configuration files from os-dependent preference location. */
@SuppressWarnings({"FinalClass", "UtilityClass", "UtilityClassCanBeEnum"})
public final class ConfigurationLoader {

  private static Path CONFIG_DIRECTORY = Paths.get("AMPT_configuration");

  private ConfigurationLoader() {}

  /**
   * Get the configuration directory for our tool.
   *
   * @return The current CONFIG_DIRECTORY
   */
  public static Path getConfigDirectory() {
    return CONFIG_DIRECTORY;
  }

  /**
   * Set the configuration directory for our tool.
   *
   * @param configDirectory The new configuration directory.
   */
  public static void setConfigDirectory(String configDirectory) {
    CONFIG_DIRECTORY = Paths.get(configDirectory);
  }

  /**
   * Set the configuration directory for our tool.
   *
   * @param configDirectory The new configuration directory.
   */
  public static void setConfigDirectory(Path configDirectory) {
    CONFIG_DIRECTORY = configDirectory;
  }

  /**
   * Load a CSV configuration file. The CSV file <b>must</b> have a header and be comma separated.
   *
   * @param filename The name of a JSON configuration file in the preferences directory.
   * @return A list of HashMaps containing the rows keyed by column name.
   * @throws ConfigurationFileLoadException If there are issues opening or reading the configuration
   *     file.
   */
  public static ArrayList<HashMap<String, String>> get_csv_file(String filename)
      throws ConfigurationFileLoadException {
    final String config_file = getFullConfigPath(filename);
    final CSVReader csv_reader = null;
    final java.util.ArrayList<java.util.HashMap<String, String>> result;
    try {
      result = Utilities.loadCSVAsMap(config_file);
    } catch (final CSVFileLoadException e) {
      throw new ConfigurationFileLoadException(String.format("Couldn't load %s", filename), e);
    }
    return result;
  }

  /**
   * Return the os dependent path to a configuration file.
   *
   * <p>If ConfigurationLoader.CONFIG_DIRECTORY is absolute simply return that value, otherwise
   * return ConfigurationLoader.CONFIG_DIRECTORY inside the os dependent configuration location.
   *
   * @param filename The name of the configuration file.
   * @return a string representation of the fill path to the config file named <code>filename</code>
   *     .
   */
  public static String getFullConfigPath(String filename) throws ConfigurationFileLoadException {
    final Path configurationFile;
    configurationFile = getAbsoluteConfigurationPath(filename);

    // If the file doesn't exist, try to grab a copy from defaults
    if (!Files.exists(configurationFile)) {
      final Path target_dir = configurationFile.getParent();

      createPreferenceDirectory(target_dir);

      final URL resource =
          ConfigurationLoader.class.getResource(String.format("/default_config/%s", filename));
      if (null == resource) {
        throw new ConfigurationFileLoadException(
            String.format("Unknown configuration file '%s'", filename), null);
      }
      try {
        Files.copy(resource.openStream(), configurationFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (final IOException e) {
        throw new ConfigurationFileLoadException(
            String.format("Couldn't copy config file %s", resource), e);
      }
    }
    return configurationFile.toString();
  }

  /**
   * Generate an absolute path to filename within the configuration directory.
   *
   * @param filename the configuration file name
   * @return the absolute path to the configuration file.
   */
  public static Path getAbsoluteConfigurationPath(String filename) {
    final Path configurationFile;
    if (CONFIG_DIRECTORY.isAbsolute()) {
      configurationFile = Paths.get(CONFIG_DIRECTORY.toString(), filename);
    } else {
      configurationFile = Paths.get(Prefs.getPrefsDir(), CONFIG_DIRECTORY.toString(), filename);
    }
    return configurationFile;
  }

  private static void createPreferenceDirectory(Path target_dir)
      throws ConfigurationFileLoadException {
    if (!Files.exists(target_dir)) {
      try {
        Files.createDirectories(target_dir);
      } catch (final IOException e) {
        throw new ConfigurationFileLoadException("Could not create config dir", e);
      }
    }
  }

  /**
   * Load a JSON configuration file.
   *
   * @param filename - The name of a JSON configuration file in the preferences directory.
   * @return A map of objects representation of a JSON file.
   * @throws ConfigurationFileLoadException If there are issues opening or reading the configuration
   *     file.
   */
  public static HashMap<String, Object> get_json_file(String filename)
      throws ConfigurationFileLoadException {
    final String config_file = getFullConfigPath(filename);
    final byte[] encoded;
    try {
      encoded = Files.readAllBytes(Paths.get(config_file));
    } catch (final IOException e) {
      throw new ConfigurationFileLoadException(
          String.format("IO problem encountered loading '%s'", config_file), e);
    }
    final String json = new String(encoded, StandardCharsets.UTF_8);
    final HashMap<String, Object> params = new HashMap<>();
    params.put(JsonReader.USE_MAPS, true);
    final Object obj = JsonReader.jsonToJava(json, params);
    //noinspection unchecked
    return (HashMap<String, Object>) obj;
  }
}
