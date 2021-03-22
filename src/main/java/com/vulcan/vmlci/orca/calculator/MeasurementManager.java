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

package com.vulcan.vmlci.orca.calculator;

import com.vulcan.vmlci.orca.data.ColumnDescriptor;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.data.Point;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;

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
    return "MeasurementConf.json";
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
                MeasurementManager.length(
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
