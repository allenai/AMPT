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

package org.allenai.allenmli.orca.ui;

import org.allenai.allenmli.orca.helpers.ConfigurationLoader;
import ij.gui.MessageDialog;

import javax.swing.JFrame;

/** Launches the informational dialog for showing the configuration file location. */
public class ConfigurationLocationDialog {
  public ConfigurationLocationDialog(JFrame owner) {
    final MessageDialog messageDialog =
        new MessageDialog(
            owner,
            "Configuration location",
            ConfigurationLoader.getAbsoluteConfigurationPath("").toString());
    messageDialog.escapePressed();
  }
}
