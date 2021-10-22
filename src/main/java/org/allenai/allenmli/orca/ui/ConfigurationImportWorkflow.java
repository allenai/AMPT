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

import org.allenai.allenmli.orca.helpers.ConfigurationManager;
import org.allenai.allenmli.orca.validator.JsonValidationException;
import ij.gui.MessageDialog;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/** Executes the workflow for importing configuration files. */
public class ConfigurationImportWorkflow {
  private final JFileChooser fileChooser;
  private final Logger logger = new StderrLogService();

  public ConfigurationImportWorkflow(JFrame owner) {
    fileChooser = new JFileChooser();
    final int result = openDialog(owner);
    final Optional<File> file = getSelectedFile(result);
    if (!file.isPresent()) {
      return;
    }

    try {
      ConfigurationManager.copyNewConfigsToPreferencesDirectory(file.get());
    } catch (IOException | JsonValidationException e) {
      logger.error("Problem encountered copying new configuration files", e);
      return;
    }

    final MessageDialog doneDialog =
        new MessageDialog(
            owner,
            "Please restart AMPT",
            "New configuration successfully imported.\nAMPT must be restarted for the new configuration to take effect.");
    doneDialog.escapePressed();
  }

  private int openDialog(JFrame owner) {
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    return fileChooser.showOpenDialog(owner);
  }

  private Optional<File> getSelectedFile(int result) {
    if (JFileChooser.APPROVE_OPTION == result) {
      return Optional.of(fileChooser.getSelectedFile());
    }
    return Optional.empty();
  }
}
