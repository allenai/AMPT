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

package com.vulcan.vmlci.orca.ui;

import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationManager;
import ij.gui.MessageDialog;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;

/** Executes the workflow for restoring default configuration files. */
public class ConfigurationRestoreWorkflow {
  private static final String CONFIRM_OVERWRITE_MESSAGE =
      "This will overwrite your existing configurations with the defaults\n\n"
          + "Do you want to proceed?";
  private final Logger logger = new StderrLogService();

  public ConfigurationRestoreWorkflow(JFrame owner) {
    final int result =
        JOptionPane.showConfirmDialog(
            owner,
            ConfigurationRestoreWorkflow.CONFIRM_OVERWRITE_MESSAGE,
            "Restore Default Configuration",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    switch (result) {
      case JOptionPane.YES_OPTION:
        try {
          ConfigurationManager.copyDefaultConfigsToPreferencesDirectory();
        } catch (ConfigurationFileLoadException | IOException e) {
          logger.error(e);
        }
        break;
      case JOptionPane.NO_OPTION:
      default:
        return;
    }

    final MessageDialog doneDialog =
        new MessageDialog(
            owner,
            "Please restart AMPT",
            "Default configuration successfully restored.\nAMPT must be restarted for the new configuration to take effect.");
    doneDialog.escapePressed();
  }
}
