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

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Comparator;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MeasurementTableModel extends AbstractTableModel {
  LastActiveImage lastActiveImage;
  String active_image = "2018-09-11 20-39-58.jpg";
  Predicate<ColumnDescriptor> filter;
  /** The rows that are appropriate to the the view. */
  private Vector<String> rows;

  private Vector<Boolean> selections;
  private DataStore dataStore;
  private int nRows;
  /**
   * Constructs a default <code>DefaultTableModel</code> which is a table of zero columns and zero
   * rows.
   */
  public MeasurementTableModel(DataStore dataStore, Predicate<ColumnDescriptor> selection_filter) {
    this.dataStore = dataStore;
    filter = selection_filter;
    System.err.println("Constructed");
    configure();
  }

  private void configure() {
    // Make a vector for JComboBox
    rows =
        dataStore.descriptors.values().stream() // Grab a stream of descriptors
            .filter(filter) // Grab the length descriptors.
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort them by descriptor index
            .map(s -> s.name) // Extract the names
            .collect(Collectors.toCollection(Vector::new));
  }

  public static void main(String[] args) {
    DataStore dataStore = null;
    try {
      dataStore =
          DataStore.createDataStore(
              new File("/Users/palbee/Development/AMPT/src/test/resources/data/sample_full.csv"));
    } catch (ConfigurationFileLoadException | DataFileLoadException e) {
      e.printStackTrace();
    }

    MeasurementTableModel mt =
        new MeasurementTableModel(
            dataStore, s -> s.measurement_type.equals("length") && !s.name.contains("%"));
    System.out.println(String.format("nRows = %d", mt.getRowCount()));
  }

  /**
   * Returns the number of rows in this data table.
   *
   * @return the number of rows in the model
   */
  @Override
  public int getRowCount() {
    //    return 0;
    return rows.size();
  }

  /**
   * Returns the number of columns in this data table.
   *
   * @return the number of columns in the model
   */
  @Override
  public int getColumnCount() {
    return 3;
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
    return null;
  }
}
