/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.ui;

import com.vulcan.vmlci.orca.data.DataStore;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Color;
import java.awt.event.WindowAdapter;

public class MeasurementTable extends WindowAdapter implements TableModelListener {
  final private DataStore dataStore;
  private JFrame frame;

  public MeasurementTable(DataStore dataStore) {
    this.dataStore = dataStore;
    build_ui();
    this.dataStore.addTableModelListener(this);
  }

  private void build_ui() {
    JTable table = new JTable(dataStore);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowGrid(true);
    table.setGridColor(Color.BLACK);
    table.doLayout();
    JScrollPane scrollPane = new JScrollPane(table);
    frame = new JFrame(dataStore.getCsvFileName());
    frame.add(scrollPane);
    frame.pack();
  }

  final public JFrame getFrame() {
    return frame;
  }

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e event from table model
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (dataStore.dirty()) {
      frame.setTitle(String.format("%s - Unsaved Changes", dataStore.getCsvFileName()));
    } else {
      frame.setTitle(dataStore.getCsvFileName());
    }
  }
}
