/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.allenai.allenmli.orca.helpers;

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
   * @param filename The name of a CSV configuration file in the preferences directory.
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
      copyDefaultConfigToPath(filename, configurationFile);
    }
    return configurationFile.toString();
  }

  /**
   * Copies the default config for the given file name to the specified path.
   *
   * <p>The file name must match that of a default config.
   *
   * @param filename The name of the default config file to copy.
   * @param configurationFile The destination location for the default config file.
   * @throws ConfigurationFileLoadException If there are any errors copying the config file.
   */
  public static void copyDefaultConfigToPath(String filename, Path configurationFile)
      throws ConfigurationFileLoadException {
    final URL resource =
        ConfigurationLoader.class.getResource(
            String.format("%s/%s", getDefaultConfigDirectory(), filename));
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

  /**
   * Returns the path to the default config directory.
   *
   * <p>In other words, this is the resources directory storing the default configs that are
   * packaged with the executable. Note that these files are read-only.
   *
   * @return A path to the directory that contains the default configuration files.
   */
  public static String getDefaultConfigDirectory() {
    return "/default_config";
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
  public static Object getJsonFile(String filename) throws ConfigurationFileLoadException {
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
    return JsonReader.jsonToJava(json, params);
  }

  /**
   * Load a JSON configuration file as a map of objects.
   *
   * @param filename - The name of a JSON configuration file in the preferences directory.
   * @return A map of objects representation of a JSON file.
   * @throws ConfigurationFileLoadException If there are issues opening or reading the configuration
   *     file.
   */
  public static HashMap<String, Object> getJsonFileAsMap(String filename)
      throws ConfigurationFileLoadException {
    //noinspection unchecked
    return (HashMap<String, Object>) getJsonFile(filename);
  }
}
