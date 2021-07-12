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

package com.vulcan.vmlci.orca.validator;

import com.vulcan.vmlci.orca.helpers.ConfigurationFile;
import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonConfigValidatorTest extends TestCase {
  private Path originalConfigPath;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    originalConfigPath = ConfigurationLoader.getConfigDirectory();
    final String testingConfigPath =
        Paths.get(JsonConfigValidatorTest.class.getResource("/measurement-tool-config/").toURI())
            .toString();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
  }

  public void test_validate_all_default_configs() throws Exception {
    final JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();
    ConfigurationLoader.setConfigDirectory(ConfigurationLoader.getDefaultConfigDirectory());
    jsonConfigValidator.validateAllConfigs();
    // No assertions needed. Simply not throwing an exception is sufficient for testing.
  }

  public void test_validate_all_test_configs() throws Exception {
    final JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();
    jsonConfigValidator.validateAllConfigs();
    // No assertions needed. Simply not throwing an exception is sufficient for testing.
  }

  public void test_validate_invalid_config() throws Exception {
    final JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();
    final Path configFilePath =
        Paths.get(
            JsonConfigValidatorTest.class
                .getResource("/measurement-tool-config/CueConfig-InvalidFormatVersion.json")
                .toURI());
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      jsonConfigValidator.validateConfig(
          configFilePath, ConfigurationFile.CUE_CONFIG.getSchemaFilename());
      TestCase.fail("Expected to fail");
    } catch (JsonValidationException e) {
      // This is potentially brittle because error messages can change and aren't subject to a spec,
      // but this is a fairly generic and minimal message so that isn't a large concern and it's
      // valuable to ensure that the error is happening for the expected reason. Apologies in
      // advance if this breaks and you have to fix it!
      MatcherAssert.assertThat(
          e.getMessage(),
          CoreMatchers.containsString("format_version: string found, integer expected"));
    }
  }

  public void test_validate_invalid_config_multiple_errors() throws Exception {
    final JsonConfigValidator jsonConfigValidator = new JsonConfigValidator();
    final Path configFilePath =
        Paths.get(
            JsonConfigValidatorTest.class
                .getResource("/measurement-tool-config/MeasurementConf-InvalidFields.json")
                .toURI());
    try {
      // TODO: Once this test has been updated to JUnit >= 4 then assertThrows can be used instead.
      jsonConfigValidator.validateConfig(
          configFilePath, ConfigurationFile.MEASUREMENT_CONFIG.getSchemaFilename());
      TestCase.fail("Expected to fail");
    } catch (JsonValidationException e) {
      // This is potentially brittle because error messages can change and aren't subject to a spec,
      // but this is a fairly generic and minimal message so that isn't a large concern and it's
      // valuable to ensure that the error is happening for the expected reason. Apologies in
      // advance if this breaks and you have to fix it!
      MatcherAssert.assertThat(
          e.getMessage(), CoreMatchers.containsString("parameters: string found, array expected"));
      MatcherAssert.assertThat(
          e.getMessage(), CoreMatchers.containsString("function: array found, string expected"));
    }
  }
}
