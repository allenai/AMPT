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

/** Existing configuration files and their properties. */
public enum ConfigurationFile {
  CUE_CONFIG("CueConfig.json", 0),
  MEASUREMENT_CONFIG("MeasurementConf.json", 0),
  REFERENCE_CONFIG("ReferenceConf.json", 0);

  /** The base name of the configuration file. */
  private final String filename;

  /**
   * The current format version of the configuration file.
   *
   * <p>Format versions must always increase monotonically.
   *
   * <p>TODO: The format version should be loaded from a resource rather than hard-coded.
   */
  private final int formatVersion;

  ConfigurationFile(String filename, int formatVersion) {
    this.filename = filename;
    this.formatVersion = formatVersion;
  }

  public String getFilename() {
    return filename;
  }

  public int getFormatVersion() {
    return formatVersion;
  }
}
