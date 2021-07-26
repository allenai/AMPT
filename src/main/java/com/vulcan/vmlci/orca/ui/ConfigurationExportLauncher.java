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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.io.File;
import java.util.Optional;

public class ConfigurationExportLauncher {
  private final JFileChooser fileChooser;

  public ConfigurationExportLauncher(JFrame owner) {
    fileChooser = new JFileChooser();
    final int result = openDialog(owner);
    final Optional<File> file = getSelectedFile(result);
    if (!file.isPresent()) {
      return;
    }
  }

  private int openDialog(JFrame owner) {
    return fileChooser.showSaveDialog(owner);
  }

  private Optional<File> getSelectedFile(int result) {
    if (JFileChooser.APPROVE_OPTION == result) {
      return Optional.of(fileChooser.getSelectedFile());
    }
    return Optional.empty();
  }
}
