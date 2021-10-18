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

package org.allenai.allenmli.orca.ui;

import org.allenai.allenmli.orca.helpers.ConfigurationManager;
import ij.gui.MessageDialog;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/** Executes the workflow for exporting configuration files. */
public class ConfigurationExportWorkflow {
  private final JFileChooser fileChooser;
  private final Logger logger = new StderrLogService();

  public ConfigurationExportWorkflow(JFrame owner) {
    fileChooser = new JFileChooser();
    final int result = saveDialog(owner);
    final Optional<File> file = getSelectedFile(result);
    if (!file.isPresent()) {
      return;
    }

    try {
      ConfigurationManager.exportConfigsFromPreferencesDirectory(file.get());
    } catch (IOException e) {
      logger.error("Problem encountered copying new configuration files", e);
      return;
    }

    final MessageDialog doneDialog =
        new MessageDialog(owner, "Configs exported", "Configuration successfully exported.");
    doneDialog.escapePressed();
  }

  private int saveDialog(JFrame owner) {
    // This will keep "All Files" as a file type filter option but make "Zip File" the default.
    FileFilter acceptAll = fileChooser.getAcceptAllFileFilter();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Zip File", "zip"));
    fileChooser.addChoosableFileFilter(acceptAll);
    return fileChooser.showSaveDialog(owner);
  }

  private Optional<File> getSelectedFile(int result) {
    if (JFileChooser.APPROVE_OPTION == result) {
      return Optional.of(fileChooser.getSelectedFile());
    }
    return Optional.empty();
  }
}
