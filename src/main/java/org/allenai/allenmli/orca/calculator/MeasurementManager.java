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

package org.allenai.allenmli.orca.calculator;

import org.allenai.allenmli.orca.data.ColumnDescriptor;
import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.Point;
import org.allenai.allenmli.orca.helpers.ConfigurationFile;
import org.allenai.allenmli.orca.helpers.ConfigurationFileLoadException;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.LinkedList;

/** Calculator targeted at updating the derived measurements in the <code>DataStore</code> */
public class MeasurementManager extends BaseCalculator implements TableModelListener {
  /**
   * @param ds the <code>DataStore</code> that the Calculator will operate on.
   * @throws ConfigurationFileLoadException when the configuration file is not present.
   */
  public MeasurementManager(DataStore ds) throws ConfigurationFileLoadException {
    super(ds);
    ds.addTableModelListener(this);
  }

  /** @return The name of the configuration file required for this calculator. */
  @Override
  protected String getConfigurationFile() {
    return ConfigurationFile.MEASUREMENT_CONFIG.getFilename();
  }

  /**
   * Respond to changes to the data_store.
   *
   * <p>This only updates the column and row(s) if a single column has been updated. If more than
   * one column has been updated, this is indicative of a file load and consequently we don't need
   * ot do any updates.
   *
   * @param event received when something has changed in the table.
   */
  @Override
  public void tableChanged(TableModelEvent event) {
    final int column = event.getColumn();
    if (TableModelEvent.ALL_COLUMNS != column) {
      final String column_name = dataStore.getColumnName(column);
      if (!"Filename".equals(column_name)) {
        for (int row = event.getFirstRow(); row <= event.getLastRow(); row++) {
          final String row_name = dataStore.getRowName(row);
          update(row_name, column_name);
        }
      }
    } else if (event.getFirstRow() != event.getLastRow()){
      // scan through all endpointColumns are endpoints and update their associated calculations.
      final ArrayList<String> endpointColumns = new ArrayList<>();
      final ArrayList<String> lengthColumns = new ArrayList<>();
      for (final ColumnDescriptor descriptor : dataStore.descriptors.values()) {
        if (descriptor.name.endsWith("_x")
            || descriptor.name.endsWith("_x_start")
            || descriptor.name.endsWith("_y")
            || descriptor.name.endsWith("_y_start")) {
          endpointColumns.add(descriptor.name);
        }
        if (descriptor.measurement_type.equals("length")) {
          lengthColumns.add(descriptor.name);
        }
      }
      final int nRows = dataStore.getRowCount();
      for (int i = 0; i < nRows; i++) {
        final String rowName = dataStore.getRowName(i);
        for (final String columnName : endpointColumns) {
          update(rowName, columnName);
        }
        for (final String columnName : lengthColumns) {
          final Point[] endpoints = dataStore.getEndpoints(rowName, columnName);
          if (null != endpoints) {
            final Double measuredLength =
                length(
                    endpoints[0].x, endpoints[0].y, endpoints[1].x, endpoints[1].y);
            if (null != measuredLength) {
              dataStore.insert_value(rowName, columnName, measuredLength);
            }
          }
        }
      }

      dataStore.setDirty(false);
    }
  }

  /**
   * Updates a measurement if data exists in the columns that contribute to it.
   *
   * <p>This method essentially does a breadth first traversal of the effect tree defined implicitly
   * in <code>BaseCalculator.possible_measurements</code>.
   *
   * @param title the name of the row being updated
   * @param column_base the base name of the column being updated.
   */
  private void update(String title, String column_base) {
    final LinkedList<String> available_measurement =
        new LinkedList<>(possible_measurements.getOrDefault(column_base, new ArrayList<>()));
    while (!available_measurement.isEmpty()) {
      final String measure = available_measurement.removeFirst();
      final Object measurement_result = do_measurement(measure, title);
      dataStore.insert_value(title, measure, measurement_result);
      available_measurement.addAll(possible_measurements.getOrDefault(measure, new ArrayList<>()));
    }
  }
}
