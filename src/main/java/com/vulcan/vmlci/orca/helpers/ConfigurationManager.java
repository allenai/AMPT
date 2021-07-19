/*
 *  Copyright (c) 2021 Vulcan Inc.
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

package com.vulcan.vmlci.orca.helpers;

import com.vulcan.vmlci.orca.validator.JsonConfigValidator;
import com.vulcan.vmlci.orca.validator.JsonValidationException;
import org.apache.commons.io.FilenameUtils;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Helpers for managing configuration files. */
@SuppressWarnings({"FinalClass", "UtilityClass", "UtilityClassCanBeEnum"})
public final class ConfigurationManager {
  private static final Logger logger = new StderrLogService();

  private static final String FORMAT_VERSION_NAME = "format_version";

  private static final String DIALOG_MESSAGE =
      "Some of your existing configuration files are out of date.\n\n"
          + "You must copy new default versions of the config files in order to continue.\n\n"
          + "A backup will be made of your existing configuration files.\n\n"
          + "Would you like to proceed?";

  private ConfigurationManager() {}

  /** Initializes all configuration files, copying over the default configs if necessary. */
  public static void initializeConfigs() {
    try {
      for (ConfigurationFile configurationFile : ConfigurationFile.values()) {
        // Retrieving the full config path will create the preferences directory and copy over the
        // default configs as necessary.
        ConfigurationLoader.getFullConfigPath(configurationFile.getFilename());
      }
    } catch (ConfigurationFileLoadException e) {
      logger.error(e);
    }
  }

  /**
   * Validates the configuration files in the user's preferences directory.
   *
   * @return True if all configs are valid, false otherwise.
   */
  public static boolean validateConfigs() {
    try {
      JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();
      jsonConfigValidator.validateAllConfigs();
      return true;
    } catch (JsonValidationException e) {
      logger.error(e);
      return false;
    }
  }

  /**
   * Checks if the format versions of all existing configuration files are up to date.
   *
   * <p>If any configuration file format versions are out of date, prompts the user to make a backup
   * of their existing configuration files and replace them with the new defaults.
   *
   * @return Whether the existing configuration files are up-to-date at the end of the method. This
   *     can be either because they were already up-to-date or because new defaults were copied to
   *     replace the previous outdated configuration files.
   */
  public static boolean checkFormatVersions() {
    final Set<ConfigurationFile> outdatedConfigs;
    try {
      outdatedConfigs = getOutdatedConfigFiles();
    } catch (ConfigurationFileLoadException e) {
      logger.error(e);
      return false;
    }

    if (!outdatedConfigs.isEmpty()) {
      final int result =
          JOptionPane.showConfirmDialog(
              null,
              DIALOG_MESSAGE,
              "Config Files Outdated",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE);
      switch (result) {
        case JOptionPane.YES_OPTION:
          try {
            backupOutdatedConfigs(outdatedConfigs);
            return true;
          } catch (IOException e) {
            logger.error(e);
            return false;
          }
        case JOptionPane.NO_OPTION:
        default:
          return false;
      }
    }
    return true;
  }

  /**
   * Returns the set of configuration files that are not up-to-date.
   *
   * <p>Precisely, this includes configuration files whose format version numbers are less than the
   * current latest format version.
   *
   * @return The set of outdated configuration files.
   * @throws ConfigurationFileLoadException If there are issues opening or reading the configuration
   *     file.
   */
  private static Set<ConfigurationFile> getOutdatedConfigFiles()
      throws ConfigurationFileLoadException {
    final Set<ConfigurationFile> outdatedConfigs = new HashSet<>();
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      if (!isAtLeastVersion(configFile.getFilename(), configFile.getFormatVersion())) {
        outdatedConfigs.add(configFile);
      }
    }
    return outdatedConfigs;
  }

  /**
   * Checks if the provided JSON configuration file is at or beyond the specified format version.
   *
   * @param filename The name of the JSON configuration file in the preferences directory to check.
   * @param expectedFormatVersion The expected format version number of the JSON configuration file.
   * @return Whether the given JSON configuration is at the expected format version.
   * @throws ConfigurationFileLoadException If there are issues opening or reading the configuration
   *     file.
   */
  static boolean isAtLeastVersion(String filename, long expectedFormatVersion)
      throws ConfigurationFileLoadException {
    long actualFormatVersion = 0; // The format version is zero if unspecified.
    final Object result = ConfigurationLoader.getJsonFile(filename);
    if (result instanceof Map) {
      //noinspection unchecked
      final Map<String, Object> map = (Map<String, Object>) result;
      if (map.containsKey(FORMAT_VERSION_NAME)) {
        final Object rawFormatVersion = map.get(FORMAT_VERSION_NAME);
        if (!(rawFormatVersion instanceof Long)) {
          throw new ConfigurationFileLoadException(
              String.format("Invalid format version '%s': %s", filename, rawFormatVersion));
        }
        actualFormatVersion = (Long) rawFormatVersion;
      }
    }
    return expectedFormatVersion <= actualFormatVersion;
  }
  /**
   * Backs up and deletes the given configuration files.
   *
   * @param outdatedConfigs The collection of configuration files to backup and delete.
   * @throws IOException If any I/O errors occur.
   */
  private static void backupOutdatedConfigs(Set<ConfigurationFile> outdatedConfigs)
      throws IOException {
    for (ConfigurationFile configFile : outdatedConfigs) {
      backupConfig(configFile.getFilename());
    }
  }

  /**
   * Moves (renames) the given configuration file to a backup.
   *
   * <p>The backup file name will include a timestamp and will have a ".bak" extension.
   *
   * @param filename The file name of a configuration file in the preferences directory.
   * @throws IOException If any I/O errors occur.
   */
  static void backupConfig(String filename) throws IOException {
    final Path configurationFile = ConfigurationLoader.getAbsoluteConfigurationPath(filename);
    if (!Files.exists(configurationFile)) {
      return;
    }

    final String base = FilenameUtils.getBaseName(configurationFile.toString());
    final String extension = FilenameUtils.getExtension(configurationFile.toString());
    final String backupFilename =
        base
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss."))
            + extension
            + ".bak";
    final Path backupConfigurationFile =
        ConfigurationLoader.getAbsoluteConfigurationPath(backupFilename);
    Files.move(configurationFile, backupConfigurationFile, StandardCopyOption.REPLACE_EXISTING);
  }
}
