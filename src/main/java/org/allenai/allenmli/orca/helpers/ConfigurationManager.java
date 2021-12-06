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

import com.google.common.collect.Sets;
import org.allenai.allenmli.orca.validator.JsonConfigValidator;
import org.allenai.allenmli.orca.validator.JsonValidationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Helpers for managing configuration files. */
@SuppressWarnings({"FinalClass", "UtilityClass", "UtilityClassCanBeEnum"})
public final class ConfigurationManager {
  private static final Logger logger = new StderrLogService();

  private static final String FORMAT_VERSION_NAME = "format_version";

  private static final String OUTDATED_CONFIG_MESSAGE =
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
    final Logger logger = new StderrLogService();

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
              OUTDATED_CONFIG_MESSAGE,
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
    if (FilenameUtils.isExtension(filename, "json")) {
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
    } else if (FilenameUtils.isExtension(filename, "csv")) {
      // There is currently no way to encode a format version for CSV files, so it is always zero.
      actualFormatVersion = 0;
    } else {
      throw new UnsupportedOperationException(
          "Unsupported extension: " + FilenameUtils.getExtension(filename));
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

  /**
   * Copies default config files to the user's preferences directory.
   *
   * <p>This is used as a restore option.
   *
   * @throws ConfigurationFileLoadException If there are any errors copying any config files.
   */
  public static void copyDefaultConfigsToPreferencesDirectory()
      throws ConfigurationFileLoadException, IOException {
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      final Path fullConfigPath =
          ConfigurationLoader.getAbsoluteConfigurationPath(configFile.getFilename());
      backupConfig(configFile.getFilename());
      ConfigurationLoader.copyDefaultConfigToPath(configFile.getFilename(), fullConfigPath);
    }
  }

  /**
   * Copies config files from the given {@link File} resource to the user's preferences directory.
   *
   * <p>The provided file may represent either a directory or a Zip file.
   *
   * @param input The input from which to copy configuration files.
   * @throws IOException If any I/O errors occur.
   * @throws JsonValidationException If any of the configuration files are invalid.
   */
  public static void copyNewConfigsToPreferencesDirectory(File input)
      throws IOException, JsonValidationException {
    final JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();

    if (input.isDirectory()) {
      final Map<String, File> fileNameToFile =
          Arrays.stream(input.listFiles())
              .collect(Collectors.toMap(File::getName, Function.identity()));
      checkForMissingConfigFiles(fileNameToFile.keySet());
      for (JsonConfigurationFile jsonConfigFile : JsonConfigurationFile.values()) {
        jsonConfigValidator.validateConfig(
            fileNameToFile.get(jsonConfigFile.getFilename()).toPath(),
            jsonConfigFile.getSchemaFilename());
      }
      for (ConfigurationFile configFile : ConfigurationFile.values()) {
        backupConfig(configFile.getFilename());
        copyToPreferencesDirectory(fileNameToFile.get(configFile.getFilename()));
      }
    } else if (Utilities.isZipFile(input)) {
      try (final ZipFile zipFile = new ZipFile(input)) {
        final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        final Map<String, ZipEntry> fileNameToZipEntry = new HashMap<>();
        while (zipEntries.hasMoreElements()) {
          final ZipEntry zipEntry = zipEntries.nextElement();
          fileNameToZipEntry.put(Paths.get(zipEntry.getName()).getFileName().toString(), zipEntry);
        }
        checkForMissingConfigFiles(fileNameToZipEntry.keySet());
        for (JsonConfigurationFile jsonConfigFile : JsonConfigurationFile.values()) {
          jsonConfigValidator.validateConfig(
              zipFile.getInputStream(fileNameToZipEntry.get(jsonConfigFile.getFilename())),
              jsonConfigFile.getSchemaFilename());
        }
        for (ConfigurationFile configFile : ConfigurationFile.values()) {
          backupConfig(configFile.getFilename());
          copyToPreferencesDirectory(
              zipFile.getInputStream(fileNameToZipEntry.get(configFile.getFilename())),
              configFile.getFilename());
        }
      }
    } else {
      throw new IllegalArgumentException(
          "Invalid config input location: " + input.getAbsolutePath());
    }
  }

  /**
   * Checks that all of the required config files are present in a given set of file names.
   *
   * @param existingFiles A set of file names. * The file names must be the last part of the path,
   *     e.g. for "/path/to/file.txt", the set should contain "file.txt".
   */
  private static void checkForMissingConfigFiles(Set<String> existingFiles) {
    final Set<String> missingConfigs =
        Sets.difference(
            Arrays.stream(ConfigurationFile.values())
                .map(ConfigurationFile::getFilename)
                .collect(Collectors.toSet()),
            existingFiles);
    if (!missingConfigs.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing required configuration file(s): " + missingConfigs);
    }
  }

  /**
   * Copies the given file to the user's preferences directory.
   *
   * @param file The file to copy.
   * @throws IOException If any I/O errors occur.
   */
  private static void copyToPreferencesDirectory(File file) throws IOException {
    final Path configurationFile = ConfigurationLoader.getAbsoluteConfigurationPath(file.getName());
    Files.copy(file.toPath(), configurationFile, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Copies the contents of the given input stream to the user's preferences directory.
   *
   * @param inputStream The input stream from which to copy.
   * @param fileName The name of the file, specifically the last part of the path, to write to in
   *     the preferences directory.
   * @throws IOException If any I/O errors occur.
   */
  private static void copyToPreferencesDirectory(InputStream inputStream, String fileName)
      throws IOException {
    final Path configurationFile = ConfigurationLoader.getAbsoluteConfigurationPath(fileName);
    FileUtils.copyInputStreamToFile(inputStream, configurationFile.toFile());
  }

  /**
   * Exports the existing configs in the user's preferences directory to the provided zip file.
   *
   * @param file A zip file to which configs should be exported.
   * @throws IOException If any I/O errors occur.
   */
  public static void exportConfigsFromPreferencesDirectory(File file) throws IOException {
    final String directoryName = "AMPT_configuration";
    final File outputFile = ensureExtension(file, "zip");
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile))) {
      zipOutputStream.putNextEntry(new ZipEntry(String.format("%s/", directoryName)));
      for (ConfigurationFile configFile : ConfigurationFile.values()) {
        zipOutputStream.putNextEntry(
            new ZipEntry(String.format("%s/%s", directoryName, configFile.getFilename())));
        zipOutputStream.write(
            Files.readAllBytes(
                ConfigurationLoader.getAbsoluteConfigurationPath(configFile.getFilename())));
        zipOutputStream.closeEntry();
      }
    }
  }

  /**
   * Ensures that the file has the appropriate extension. If it doesn't then add it.
   *
   * @param file The file to which for the provided extension.
   * @param extension The extension to check for.
   * @return A file object with the provided extension.
   */
  private static File ensureExtension(File file, String extension) {
    String fileName = file.getName();
    if (!FilenameUtils.isExtension(fileName, extension)) {
      fileName += "." + extension;
      return new File(file.getParentFile(), fileName);
    }
    return file;
  }
}
