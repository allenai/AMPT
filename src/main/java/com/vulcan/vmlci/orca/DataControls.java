/*
 * Copyright Vulcan Inc. 2021
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.vulcan.vmlci.orca;

import ij.gui.MessageDialog;
import ij.gui.YesNoCancelDialog;
import ij.io.OpenDialog;
import ij.io.SaveDialog;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;

/**
 * The <code>DataControls</code> class wraps the parts of the UI responsible for accessing the CSV
 * files.
 */
public class DataControls extends JPanel {
  final int LOAD = 0;
  final int VIEW = 1;
  final int SAVE = 2;
  final int EXPORT = 3;
  private final DataStore ds;
  JButton[] controls = {null, null, null, null};
  MeasurementTable measurement_table;
  /**
   * Creates a new <code>DataControls</code>.
   *
   * @param ds the <code>DataStore</code> that will be used for calculations.
   */
  public DataControls(DataStore ds) {
    this.ds = ds;
    build_ui();
    measurement_table = new MeasurementTable(this.ds);
    provision_load_button();
    provision_view_button();
    provision_save_button();
    provision_export_button();
  }

  private void build_ui() {
    GridBagConstraints gbc;
    this.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    controls[LOAD] = new JButton();
    controls[LOAD].setText("Load CSV");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(controls[LOAD], gbc);
    controls[VIEW] = new JButton();
    controls[VIEW].setText("View CSV");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(controls[VIEW], gbc);
    controls[SAVE] = new JButton();
    controls[SAVE].setText("Save CSV");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(controls[SAVE], gbc);
    controls[EXPORT] = new JButton();
    controls[EXPORT].setText("Export CSV");
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(controls[EXPORT], gbc);
    final JPanel spacer1 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer1, gbc);
    final JPanel spacer2 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer2, gbc);
    final JPanel spacer3 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer3, gbc);
  }

  private void provision_load_button() {
    controls[LOAD].addActionListener(
        e -> {
          if (ds.dirty()) {
            if (!DataControls.this.save(false, false)) {
              return;
            }
          }
          OpenDialog load_dialog = new OpenDialog("select a file");
          String load_directory = load_dialog.getDirectory();
          String load_filename = load_dialog.getFileName();
          if (load_directory != null && load_filename != null) {
            try {
              ds.loadData(new File(load_directory, load_filename));
            } catch (DataFileLoadException f) {
              MessageDialog errDialog =
                  new MessageDialog(null, "Error Loading CSV", f.getMessage());
              errDialog.escapePressed();
            }
          }
        });
  }

  private void provision_view_button() {
    controls[VIEW].addActionListener(
        e -> {
          JFrame frame = measurement_table.getFrame();
          if (!frame.isVisible()) {
            frame.setVisible(true);
          } else {
            int state = frame.getExtendedState();
            if ((state & JFrame.ICONIFIED) > 0) {
              frame.setExtendedState(state & ~JFrame.ICONIFIED);
            }
          }
        });
  }

  private void provision_save_button() {
    controls[SAVE].addActionListener(e -> save(true, false));
  }

  private void provision_export_button() {
    controls[EXPORT].addActionListener(e -> save(true, true));
  }

  private boolean save(boolean ignore_dirty, boolean export) {
    if (ignore_dirty || ds.dirty()) {
      File csvFile = ds.getCsvFile();
      SaveDialog saveDialog;
      if (csvFile != null) {
        saveDialog =
            new SaveDialog("Save Results As CSV", csvFile.getParent(), csvFile.getName(), ".csv");
      } else {
        saveDialog = new SaveDialog("Save Results As CSV", "", ".csv");
      }
      String directory = saveDialog.getDirectory();
      String filename = saveDialog.getFileName();
      if (directory != null && filename != null) {
        try {
          ds.save_as_csv(new File(directory, filename), export);
        } catch (IOException e) {
          MessageDialog errDialog = new MessageDialog(null, "Error Saving", e.getMessage());
          return false;
        }
      } else {
        YesNoCancelDialog discard =
            new YesNoCancelDialog(
                null, "Discard Changes", "Do you really want to discard changes?");
        return discard.yesPressed();
      }
    }
    return true;
  }
}
