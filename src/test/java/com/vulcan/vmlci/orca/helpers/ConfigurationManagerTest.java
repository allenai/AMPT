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

import com.vulcan.vmlci.orca.data.DataStoreTest;
import junit.framework.TestCase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigurationManagerTest extends TestCase {
  private Path originalConfigPath;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    originalConfigPath = ConfigurationLoader.getConfigDirectory();
    final String testingConfigPath =
        Paths.get(DataStoreTest.class.getResource("/measurement-tool-config/").toURI()).toString();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
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

    tempDirectory.toFile().deleteOnExit();
  }

  public void test_backup_nonexistent_config() throws Exception {
    final String configFilename = "ThisFileDoesNotExist.json";
    final Path fullConfigPath = ConfigurationLoader.getAbsoluteConfigurationPath(configFilename);
    ConfigurationManager.backupConfig(configFilename);
    TestCase.assertFalse(Files.exists(fullConfigPath));
  }

  public void test_original_cue_config_format() throws Exception {
    TestCase.assertTrue(ConfigurationManager.isAtLeastVersion("CueConfig-OriginalFormat.json", 0));
  }

  public void test_cue_config_with_format_version() throws Exception {
    TestCase.assertTrue(ConfigurationManager.isAtLeastVersion("CueConfig.json", 1));
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
    TestCase.assertTrue(ConfigurationManager.isAtLeastVersion("MeasurementConf.json", 0));
  }

  public void test_original_reference_conf_format() throws Exception {
    TestCase.assertTrue(ConfigurationManager.isAtLeastVersion("ReferenceConf.json", 0));
  }
}
