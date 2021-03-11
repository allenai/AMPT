/*
 * Copyright Vulcan Inc. 2021
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.vulcan.vmlci.orca.helpers;

import com.cedarsoftware.util.io.JsonReader;
import com.opencsv.CSVReader;
import ij.Prefs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/** Helpers for loading configuration files from os-dependent preference location. */
public class ConfigurationLoader {
  private static Path CONFIG_DIRECTORY = Paths.get("measurement-tool-config");

  /**
   * Get the configuration directory for our tool.
   *
   * @return The current CONFIG_DIRECTORY
   */
  public static Path getConfigDirectory() {
    return ConfigurationLoader.CONFIG_DIRECTORY;
  }

  /**
   * Set the configuration directory for our tool.
   *
   * @param configDirectory The new configuration directory.
   */
  public static void setConfigDirectory(String configDirectory) {
    ConfigurationLoader.CONFIG_DIRECTORY = Paths.get(configDirectory);
  }

  /**
   * Set the configuration directory for our tool.
   *
   * @param configDirectory The new configuration directory.
   */
  public static void setConfigDirectory(Path configDirectory) {
    ConfigurationLoader.CONFIG_DIRECTORY = configDirectory;
  }

  public static void main(String[] args) throws ConfigurationFileLoadException {
    System.out.println(ConfigurationLoader.getFullConfigPath("foo"));
    System.out.println(ConfigurationLoader.get_csv_file("CSV-Columns.csv"));
    System.out.println(ConfigurationLoader.get_json_file("TestCueConfig.json"));
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
  public static String getFullConfigPath(String filename) {
    if (ConfigurationLoader.CONFIG_DIRECTORY.isAbsolute()) {
      return Paths.get(ConfigurationLoader.CONFIG_DIRECTORY.toString(), filename).toString();
    }
    String config_location = Prefs.getPrefsDir();
    Path file_path =
        Paths.get(config_location, ConfigurationLoader.CONFIG_DIRECTORY.toString(), filename);
    return file_path.toString();
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
    String config_file = ConfigurationLoader.getFullConfigPath(filename);
    CSVReader csv_reader = null;
    java.util.ArrayList<java.util.HashMap<String, String>> result;
    try {
      result = Utilities.loadCSVAsMap(config_file);
    } catch (CSVFileLoadException e) {
      throw new ConfigurationFileLoadException(String.format("Couldn't load %s", filename), e);
    }
    return result;
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
    String config_file = ConfigurationLoader.getFullConfigPath(filename);
    byte[] encoded;
    try {
      encoded = Files.readAllBytes(Paths.get(config_file));
    } catch (IOException e) {
      throw new ConfigurationFileLoadException(
          String.format("IO problem encountered loading '%s'", config_file), e);
    }
    String json = new String(encoded, StandardCharsets.UTF_8);
    HashMap<String, Object> params = new HashMap<>();
    params.put(JsonReader.USE_MAPS, true);
    Object obj = JsonReader.jsonToJava(json, params);
    return (HashMap<String, Object>) obj;
  }
}
