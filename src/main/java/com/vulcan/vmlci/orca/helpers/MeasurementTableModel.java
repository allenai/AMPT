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

package com.vulcan.vmlci.orca.helpers;

import com.vulcan.vmlci.orca.data.ColumnDescriptor;
import com.vulcan.vmlci.orca.data.DataStore;

import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MeasurementTableModel extends AbstractTableModel implements TableModelListener {
  LastActiveImage lastActiveImage;
  Predicate<ColumnDescriptor> filter;
  /** The rows that are appropriate to the the view. */
  private Vector<ColumnDescriptor> rows;

  private Vector<Boolean> selections;
  private HashSet<String> needs_validation;
  private DataStore dataStore;
  /** This hashmap converts a column index into the corresponding row index in out model. */
  private HashMap<Integer, Integer> dataStoreColumnToLocalRow;

  private int nRows;
  /**
   * Constructs a default <code>DefaultTableModel</code> which is a table of zero columns and zero
   * rows.
   */
  public MeasurementTableModel(DataStore dataStore, Predicate<ColumnDescriptor> selection_filter) {
    this.dataStore = dataStore;
    filter = selection_filter;
    lastActiveImage = LastActiveImage.getInstance();
    configure();
    this.dataStore.addTableModelListener(this);
    lastActiveImage.addActiveImageListener(e -> this.fireTableDataChanged());
  }

  private void configure() {
    // Make a vector for JComboBox
    rows =
        dataStore.descriptors.values().stream() // Grab a stream of descriptors
            .filter(filter) // Grab the length descriptors.
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort them by descriptor index
            //            .map(s -> s.name) // Extract the names
            .collect(Collectors.toCollection(Vector::new));
    selections = new Vector<>();
    dataStoreColumnToLocalRow = new HashMap<>();
    needs_validation = new HashSet<>();
    for (int i = 0; i < rows.size(); i++) {
      selections.add(false);
      dataStoreColumnToLocalRow.put(rows.get(i).index, i);
      String target = String.format("%s_reviewed", rows.get(i).name);
      if (dataStore.descriptors.containsKey(target)){
        needs_validation.add(target);
      }
    }
  }

  /**
   * Returns a default name for the column using spreadsheet conventions: A, B, C, ... Z, AA, AB,
   * etc. If <code>column</code> cannot be found, returns an empty string.
   *
   * @param column the column being queried
   * @return a string containing the default name of <code>column</code>
   */
  @Override
  public String getColumnName(int column) {
    return new String[] {"Render", "Measurement", "Value", "Status"}[column];
  }

  /**
   * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
   *
   * @param columnIndex the column being queried
   * @return the Object.class
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Boolean.class;
      case 3:
      case 1:
        return String.class;
      default:
        return Double.class;
    }
  }

  /**
   * Notifies all listeners that all cell values in the table's rows may have changed. The number of
   * rows may also have changed and the <code>JTable</code> should redraw the table from scratch.
   * The structure of the table (as in the order of the columns) is assumed to be the same.
   *
   * @see TableModelEvent
   * @see EventListenerList
   * @see JTable#tableChanged(TableModelEvent)
   */
  @Override
  public void fireTableDataChanged() {
    super.fireTableDataChanged();
  }

  /**
   * Returns the number of rows in this data table.
   *
   * @return the number of rows in the model
   */
  @Override
  public int getRowCount() {
    return rows.size();
  }

  /**
   * Returns the number of columns in this data table.
   *
   * @return the number of columns in the model
   */
  @Override
  public int getColumnCount() {
    return 4;
  }

  /**
   * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
   *
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return selections.get(rowIndex);
      case 1:
        return rows.get(rowIndex).name;
      case 2:
        return dataStore.get_value(
            lastActiveImage.getMostRecentImageName(), rows.get(rowIndex).name);
      default:
        if (lastActiveImage.getMostRecentImageName().equals(LastActiveImage.NO_OPEN_IMAGE)) {
          return "";
        }
        String target_row = String.format("%s_reviewed", rows.get(rowIndex).name);
        if (!needs_validation.contains(target_row)) {
          return DataStore.NAStatus;
        }
        Object result = dataStore.get_value(lastActiveImage.getMostRecentImageName(), target_row);
        if (result == null) {
          return DataStore.UNREVIEWED;
        } else {
          return DataStore.ACCEPTED;
        }
    }
  }

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    Integer row = dataStoreColumnToLocalRow.get(e.getColumn());
    if (row != null) {
      fireTableRowsUpdated(row, row);
    }
  }
}
