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

import org.allenai.allenmli.orca.data.ColumnDescriptor;
import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.MeasurementTableModel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
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
    setLayout(new GridBagLayout());
    GridBagConstraints gbc;

    // Render Checkbox
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    renderCheckBox = new JCheckBox();
    renderCheckBox.setText("Render");
    add(renderCheckBox, gbc);

    // Select All
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    selectAllButton = new JButton();
    selectAllButton.setText("Select All");
    add(selectAllButton, gbc);

    // Clear All
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    clearAllButton = new JButton();
    clearAllButton.setText("Clear All");
    add(clearAllButton, gbc);

    // Top row spacer
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    // ScrollPane with Table
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 4;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;

    final JTable table = new JTable(myModel);
    final Dimension viewportDimension = table.getPreferredScrollableViewportSize();
    int nRow = myModel.getRowCount();
    if (10 < nRow) {
      nRow = 10;
    }
    viewportDimension.height = table.getRowHeight() * nRow;
    table.setShowGrid(true);
    table.setGridColor(Color.BLACK);
    table.setPreferredScrollableViewportSize(viewportDimension);
    table.setFillsViewportHeight(true);

    add(new JScrollPane(table), gbc);
    revalidate();
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
   * @param e The event encoding the change to the table.
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
