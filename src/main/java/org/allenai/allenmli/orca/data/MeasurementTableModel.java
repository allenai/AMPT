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

package org.allenai.allenmli.orca.data;

import org.allenai.allenmli.orca.helpers.LastActiveImage;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MeasurementTableModel extends AbstractTableModel implements TableModelListener {
  private final DataStore dataStore;
  private final LastActiveImage lastActiveImage = LastActiveImage.getInstance();
  Predicate<ColumnDescriptor> filter;
  /** The rows that are appropriate to the the view. */
  private ArrayList<ColumnDescriptor> rows;

  private ArrayList<Boolean> selections;
  private HashSet<String> needs_validation;
  /** This hashmap converts a column index into the corresponding row index in out model. */
  private HashMap<Integer, Integer> dataStoreColumnToLocalRow;

  private int nRows;

  /**
   * Constructs a default <code>DefaultTableModel</code> which is a table of zero columns and zero
   * rows.
   */
  public MeasurementTableModel(DataStore dataStore, Predicate<ColumnDescriptor> selection_filter) {
    this.dataStore = dataStore;
    configure(selection_filter);
    this.dataStore.addTableModelListener(this);
    lastActiveImage.addActiveImageListener(e -> this.fireTableDataChanged());
  }

  /**
   * Construct the selections collection based in filter and the contents of the dataStore.
   *
   * @param filter predicate for selecting rows.
   */
  private void configure(Predicate<? super ColumnDescriptor> filter) {
    // Make a vector for JComboBox
    rows =
        dataStore.descriptors.values().stream() // Grab a stream of descriptors
            .filter(filter) // Grab the length descriptors.
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort them by descriptor index
            .collect(Collectors.toCollection(ArrayList::new));
    selections = new ArrayList<>();
    dataStoreColumnToLocalRow = new HashMap<>();
    needs_validation = new HashSet<>();
    for (int i = 0; i < rows.size(); i++) {
      selections.add(false);
      dataStoreColumnToLocalRow.put(rows.get(i).index, i);
      String target = String.format("%s_reviewed", rows.get(i).name);
      if (dataStore.descriptors.containsKey(target)) {
        dataStoreColumnToLocalRow.put(dataStore.descriptors.get(target).index, i);
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
   * Returns true for the Render column, false otherwise.
   *
   * @param rowIndex the row being queried
   * @param columnIndex the column being queried
   * @return true for column 0, false otherwise.
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (0 == columnIndex);
  }

  /**
   * Updates the value of the selections ArrayList to indicate which measurements should be drawn.
   *
   * @param aValue value to assign to cell
   * @param rowIndex row of cell
   * @param columnIndex column of cell
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (0 == columnIndex) {
      selections.set(rowIndex, (Boolean) aValue);
      this.fireTableCellUpdated(rowIndex, columnIndex);
    } else {
      super.setValueAt(aValue, rowIndex, columnIndex);
    }
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
        if (lastActiveImage.no_images()) {
          return "";
        }
        String target_row = String.format("%s_reviewed", rows.get(rowIndex).name);
        if (!needs_validation.contains(target_row)) {
          return DataStore.NAStatus;
        }
        Boolean result =
            dataStore.get_value(
                lastActiveImage.getMostRecentImageName(), target_row, Boolean.class, null);
        if (null == result || !result) {
          return DataStore.UNREVIEWED;
        } else {
          return DataStore.ACCEPTED;
        }
    }
  }

  /**
   * Event handler to alert on changes from the underlying data store.
   *
   * @param e change event from the datastore
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    Integer row = dataStoreColumnToLocalRow.get(e.getColumn());
    if (null != row) {
      fireTableRowsUpdated(row, row);
    }
  }

  public void setAll(boolean newState) {
    int bound = selections.size();
    IntStream.range(0, bound).forEach(i -> selections.set(i, newState));
    fireTableRowsUpdated(0, selections.size() - 1);
  }
}
