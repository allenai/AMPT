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

import org.allenai.allenmli.orca.validator.JsonValidationException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ConfigurationManagerTest extends TestCase {
  private Path originalConfigPath;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    originalConfigPath = ConfigurationLoader.getConfigDirectory();
    final String testingConfigPath =
        Paths.get(ConfigurationManagerTest.class.getResource("/measurement-tool-config/").toURI())
            .toString();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
  }

  public void test_initialize_configs() throws Exception {
    final Path tempDirectory = Files.createTempDirectory("scratch");
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    ConfigurationManager.initializeConfigs();
    final String defaultConfigDirectory =
        Paths.get(
                ConfigurationLoader.class
                    .getResource(ConfigurationLoader.getDefaultConfigDirectory())
                    .toURI())
            .toString();
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      TestCase.assertTrue(
          FileUtils.contentEquals(
              new File(defaultConfigDirectory, configFile.getFilename()),
              new File(tempDirectory.toFile(), configFile.getFilename())));
    }
    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  public void test_backup_config() throws Exception {
    final String configFilename = "MeasurementConf.json";
    final Path existingConfig = ConfigurationLoader.getAbsoluteConfigurationPath(configFilename);
    final Path tempDirectory = Files.createTempDirectory("scratch");
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    final Path newConfig = Paths.get(tempDirectory.toString(), configFilename);
    Files.copy(existingConfig, newConfig, StandardCopyOption.REPLACE_EXISTING);

    TestCase.assertTrue(Files.exists(newConfig));
    ConfigurationManager.backupConfig(configFilename);

    // The config file should be moved (renamed) to a backup.
    TestCase.assertFalse(Files.exists(newConfig));
    final String[] files = tempDirectory.toFile().list();
    TestCase.assertEquals(1, files.length);
    TestCase.assertTrue(files[0].matches("^MeasurementConf-.*\\.json\\.bak$"));

    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  public void test_backup_nonexistent_config() throws Exception {
    final String configFilename = "ThisFileDoesNotExist.json";
    final Path fullConfigPath = ConfigurationLoader.getAbsoluteConfigurationPath(configFilename);
    ConfigurationManager.backupConfig(configFilename);
    TestCase.assertFalse(Files.exists(fullConfigPath));
  }

  public void test_original_cue_config_format() throws Exception {
    assertTrue(ConfigurationManager.isAtLeastVersion("CueConfig-OriginalFormat.json", 0));
  }

  public void test_cue_config_with_format_version() throws Exception {
    assertTrue(ConfigurationManager.isAtLeastVersion("CueConfig.json", 1));
  }

  @SuppressWarnings("EmptyCatchBlock")
  public void test_cue_config_with_invalid_format_version() {
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.isAtLeastVersion("CueConfig-InvalidFormatVersion.json", 0);
      TestCase.fail("Expected to fail");
    } catch (ConfigurationFileLoadException e) {
    }
  }

  public void test_original_measurement_conf_format() throws Exception {
    assertTrue(ConfigurationManager.isAtLeastVersion("MeasurementConf.json", 0));
  }

  public void test_original_reference_conf_format() throws Exception {
    assertTrue(ConfigurationManager.isAtLeastVersion("ReferenceConf.json", 0));
  }

  public void test_copy_new_configs_from_directory() throws Exception {
    final Path tempDirectory = Files.createTempDirectory("scratch");
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    final File testingConfigDirectory =
        new File(ConfigurationManagerTest.class.getResource("/measurement-tool-config/").toURI());
    ConfigurationManager.copyNewConfigsToPreferencesDirectory(testingConfigDirectory);
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      TestCase.assertTrue(
          FileUtils.contentEquals(
              new File(testingConfigDirectory, configFile.getFilename()),
              new File(tempDirectory.toFile(), configFile.getFilename())));
    }
    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  public void test_copy_new_configs_from_zip_file() throws Exception {
    final Path tempDirectory = Files.createTempDirectory("scratch");
    final File tempZipFile = Files.createTempFile("scratch", ".zip").toFile();
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    final File testingConfigDirectory =
        new File(ConfigurationManagerTest.class.getResource("/measurement-tool-config/").toURI());
    // Create a zip file containing the config files.
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
      for (ConfigurationFile configFile : ConfigurationFile.values()) {
        zipOutputStream.putNextEntry(new ZipEntry(configFile.getFilename()));
        zipOutputStream.write(
            Files.readAllBytes(
                Paths.get(testingConfigDirectory.getAbsolutePath(), configFile.getFilename())));
        zipOutputStream.closeEntry();
      }
    }
    ConfigurationManager.copyNewConfigsToPreferencesDirectory(tempZipFile);
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      TestCase.assertTrue(
          FileUtils.contentEquals(
              new File(testingConfigDirectory, configFile.getFilename()),
              new File(tempDirectory.toFile(), configFile.getFilename())));
    }
    FileUtils.deleteDirectory(tempDirectory.toFile());
    tempZipFile.deleteOnExit();
  }

  public void test_copy_new_configs_from_invalid_file() throws Exception {
    final File tempFile = Files.createTempFile("scratch", ".txt").toFile();
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(tempFile);
      TestCase.fail("Expected to fail");
    } catch (IllegalArgumentException e) {
      MatcherAssert.assertThat(
          e.getMessage(), CoreMatchers.containsString("Invalid config input location"));
    }
    tempFile.deleteOnExit();
  }

  public void test_copy_new_configs_from_empty_directory() throws Exception {
    final Path inputDirectory = Files.createTempDirectory("input");
    final Path outputDirectory = Files.createTempDirectory("output");
    ConfigurationLoader.setConfigDirectory(outputDirectory);
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(inputDirectory.toFile());
      TestCase.fail("Expected to fail");
    } catch (IllegalArgumentException e) {
      MatcherAssert.assertThat(
          e.getMessage(), CoreMatchers.containsString("Missing required configuration file(s)"));
    }
    FileUtils.deleteDirectory(inputDirectory.toFile());
    FileUtils.deleteDirectory(outputDirectory.toFile());
  }

  public void test_copy_new_configs_from_zip_file_missing_configs() throws Exception {
    final Path tempDirectory = Files.createTempDirectory("scratch");
    final File tempZipFile = Files.createTempFile("scratch", ".zip").toFile();
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    // Create a dummy, but valid zip file.
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
      zipOutputStream.putNextEntry(new ZipEntry("foo"));
      zipOutputStream.write("bar".getBytes(StandardCharsets.UTF_8));
      zipOutputStream.closeEntry();
    }
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(tempZipFile);
      TestCase.fail("Expected to fail");
    } catch (IllegalArgumentException e) {
      MatcherAssert.assertThat(
          e.getMessage(), CoreMatchers.containsString("Missing required configuration file(s)"));
    }
    FileUtils.deleteDirectory(tempDirectory.toFile());
    tempZipFile.deleteOnExit();
  }

  public void test_copy_new_invalid_config_from_directory() throws Exception {
    final Path inputDirectory = Files.createTempDirectory("input");
    final Path outputDirectory = Files.createTempDirectory("output");
    ConfigurationLoader.setConfigDirectory(outputDirectory);
    final File testingConfigDirectory =
        new File(ConfigurationManagerTest.class.getResource("/measurement-tool-config/").toURI());
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      Files.copy(
          new File(testingConfigDirectory, configFile.getFilename()).toPath(),
          new File(inputDirectory.toFile(), configFile.getFilename()).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    }
    // Use an invalid config file, i.e. one that won't pass JSON validation.
    Files.copy(
        new File(testingConfigDirectory, "CueConfig-InvalidFormatVersion.json").toPath(),
        new File(inputDirectory.toFile(), ConfigurationFile.CUE_CONFIG.getFilename()).toPath(),
        StandardCopyOption.REPLACE_EXISTING);
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(inputDirectory.toFile());
      TestCase.fail("Expected to fail");
    } catch (JsonValidationException e) {
    }
    FileUtils.deleteDirectory(inputDirectory.toFile());
    FileUtils.deleteDirectory(outputDirectory.toFile());
  }

  public void test_copy_new_invalid_config_from_zip_file() throws Exception {
    final Path inputDirectory = Files.createTempDirectory("input");
    final Path outputDirectory = Files.createTempDirectory("output");
    final File tempZipFile = Files.createTempFile("scratch", ".zip").toFile();
    ConfigurationLoader.setConfigDirectory(outputDirectory);
    final File testingConfigDirectory =
        new File(ConfigurationManagerTest.class.getResource("/measurement-tool-config/").toURI());
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      Files.copy(
          new File(testingConfigDirectory, configFile.getFilename()).toPath(),
          new File(inputDirectory.toFile(), configFile.getFilename()).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    }
    // Use an invalid config file, i.e. one that won't pass JSON validation.
    Files.copy(
        new File(testingConfigDirectory, "CueConfig-InvalidFormatVersion.json").toPath(),
        new File(inputDirectory.toFile(), ConfigurationFile.CUE_CONFIG.getFilename()).toPath(),
        StandardCopyOption.REPLACE_EXISTING);
    // Create a zip file containing the config files.
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
      for (ConfigurationFile configFile : ConfigurationFile.values()) {
        zipOutputStream.putNextEntry(new ZipEntry(configFile.getFilename()));
        zipOutputStream.write(
            Files.readAllBytes(Paths.get(inputDirectory.toString(), configFile.getFilename())));
        zipOutputStream.closeEntry();
      }
    }
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(inputDirectory.toFile());
      TestCase.fail("Expected to fail");
    } catch (JsonValidationException e) {
    }
    FileUtils.deleteDirectory(inputDirectory.toFile());
    FileUtils.deleteDirectory(outputDirectory.toFile());
    tempZipFile.deleteOnExit();
  }

  public void test_copy_default_configs() throws Exception {
    final Path tempDirectory = Files.createTempDirectory("scratch");
    ConfigurationLoader.setConfigDirectory(tempDirectory);
    ConfigurationManager.copyDefaultConfigsToPreferencesDirectory();
    final File defaultConfigDirectory =
        new File(ConfigurationLoader.class.getResource("/default_config/").toURI());
    for (ConfigurationFile configFile : ConfigurationFile.values()) {
      TestCase.assertTrue(
          FileUtils.contentEquals(
              new File(defaultConfigDirectory, configFile.getFilename()),
              new File(tempDirectory.toFile(), configFile.getFilename())));
    }
    TestCase.assertTrue(
        FileUtils.contentEquals(
            new File(defaultConfigDirectory, "CSV-Columns.csv"),
            new File(tempDirectory.toFile(), "CSV-Columns.csv")));
    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  public void test_export_configs_from_preferences_directory() throws Exception {
    final File tempZipFile = Files.createTempFile("scratch", ".zip").toFile();
    ConfigurationManager.exportConfigsFromPreferencesDirectory(tempZipFile);
    try (final ZipFile zipFile = new ZipFile(tempZipFile)) {
      final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
      final Map<String, ZipEntry> fileNameToZipEntry = new HashMap<>();
      while (zipEntries.hasMoreElements()) {
        final ZipEntry zipEntry = zipEntries.nextElement();
        fileNameToZipEntry.put(Paths.get(zipEntry.getName()).getFileName().toString(), zipEntry);
      }
      for (ConfigurationFile configFile : ConfigurationFile.values()) {
        TestCase.assertTrue(
            IOUtils.contentEquals(
                new FileInputStream(
                    new File(
                        ConfigurationLoader.getConfigDirectory().toFile(),
                        configFile.getFilename())),
                zipFile.getInputStream(fileNameToZipEntry.get(configFile.getFilename()))));
      }
      TestCase.assertTrue(
          IOUtils.contentEquals(
              new FileInputStream(
                  new File(ConfigurationLoader.getConfigDirectory().toFile(), "CSV-Columns.csv")),
              zipFile.getInputStream(fileNameToZipEntry.get("CSV-Columns.csv"))));
    }
    tempZipFile.deleteOnExit();
  }
}
