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

package com.vulcan.vmlci.orca.ui;

import com.vulcan.vmlci.orca.data.ColumnDescriptor;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.data.MeasurementTableModel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Predicate;

public class LengthDisplay extends JPanel implements TableModelListener {

  private final MeasurementTableModel measurementTableModel;
  private final CueManager cueManager;
  private JButton clearAllButton;
  private JButton selectAllButton;
  private JCheckBox renderCheckBox;

  public LengthDisplay(
      DataStore dataStore, Predicate<ColumnDescriptor> selection_filter, CueManager cueManager) {
    measurementTableModel = new MeasurementTableModel(dataStore, selection_filter);
    this.cueManager = cueManager;
    build_ui(measurementTableModel);
    wire_ui();
  }

  private void build_ui(TableModel myModel) {
    this.setLayout(new GridBagLayout());
    final JScrollPane scrollPane1 = new JScrollPane();
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 4;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    this.add(scrollPane1, gbc);
    JTable table = new JTable(myModel);
    table.setShowGrid(true);
    table.setGridColor(Color.BLACK);
    scrollPane1.setViewportView(table);
    renderCheckBox = new JCheckBox();
    renderCheckBox.setText("Render");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(renderCheckBox, gbc);
    selectAllButton = new JButton();
    selectAllButton.setText("Select All");
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(selectAllButton, gbc);
    clearAllButton = new JButton();
    clearAllButton.setText("Clear All");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(clearAllButton, gbc);
    final JPanel spacer1 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer1, gbc);
  }

  private void wire_ui() {
    measurementTableModel.addTableModelListener(this);
    renderCheckBox.setModel(cueManager.overlayToggle);
    renderCheckBox.addActionListener(e -> cueManager.draw());
    selectAllButton.addActionListener(e -> measurementTableModel.setAll(true));
    clearAllButton.addActionListener(e -> measurementTableModel.setAll(false));
  }

  /**
   * Update which elements should be drawn.
   *
   * @param e
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (TableModelEvent.UPDATE == e.getType()) {
      int start = e.getFirstRow();
      int end = e.getLastRow();
      if (TableModelEvent.ALL_COLUMNS == e.getColumn()) {
        start = 0;
        end = measurementTableModel.getRowCount() - 1;
      }
      for (int i = start; i <= end; i++) {
        cueManager.setConditionLine(
            (String) measurementTableModel.getValueAt(i, 1),
            (boolean) measurementTableModel.getValueAt(i, 0));
      }
    }
    cueManager.draw();
  }
}
