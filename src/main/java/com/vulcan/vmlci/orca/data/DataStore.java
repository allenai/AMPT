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

package com.vulcan.vmlci.orca.data;

import com.opencsv.CSVWriter;
import com.vulcan.vmlci.orca.helpers.CSVFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.helpers.DataFileLoadException;
import com.vulcan.vmlci.orca.helpers.Point;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.vulcan.vmlci.orca.helpers.Utilities.loadCSVAsMap;
import static java.util.Collections.addAll;

public class DataStore extends AbstractTableModel {
  public static final String NAStatus = "N/A";
  public static final String UNREVIEWED = "Unreviewed";
  public static final String ACCEPTED = "Accepted";
  // Formatting constants.
  private static final String X_COL = "%s_x";
  private static final String Y_COL = "%s_y";
  private static final String X_START_LENGTH = "%s_x_start";
  private static final String Y_START_LENGTH = "%s_y_start";
  private static final String X_END_LENGTH = "%s_x_end";
  private static final String Y_END_LENGTH = "%s_y_end";
  private static DataStore datastore_instance = null;
  /** Metadata about each column. */
  public final HashMap<String, ColumnDescriptor> descriptors = new java.util.HashMap<>();

  /**
   * The actual data store. Currently this a list of hashmaps, but may benefit from being converted
   * to a list of lists.
   */
  private final ArrayList<HashMap<String, Object>> data = new ArrayList<>();

  /** Integer measurement units. */
  public HashSet<String> INTEGER_UNITS;

  /** Text measurement units. */
  public HashSet<String> TEXT_UNITS;

  /** Real valued measurement units. */
  public HashSet<String> FLOAT_UNITS;

  /** Measurement units that can be edited. */
  public HashSet<String> EDITABLE;

  /** Measurement units that correspond to lengths */
  public HashSet<String> FETCHABLE_LENGTHS;

  /** Measurement units that correspond to points */
  public HashSet<String> FETCHABLE_POINTS;

  public HashMap<String, Class<?>> UNIT_CLASSES;
  public HashSet<String> BOOLEAN_UNITS;

  /** The working csv file. */
  private File csvFile;
  /** Mapping to provide a quick lookup from tracked filename to row index. */
  private HashMap<String, Integer> rowMap = null;
  /** Mapping to provide a quick lookup from column index to column name. */
  private String[] columnMap = null;
  /** Track the dirty state of the data store. */
  private boolean dataDirty = false;

  /**
   * Constructs an empty DataStore instance.
   *
   * @throws ConfigurationFileLoadException This will never be thrown as a file won't be loaded.
   * @throws DataFileLoadException This will never be thrown as a file won't be loaded.
   */
  private DataStore() throws ConfigurationFileLoadException, DataFileLoadException {
    this(null);
  }

  /**
   * Construct a DataStore instance populated with data from <code>dataFile</code>
   *
   * @param dataFile the file to load data from.
   * @throws ConfigurationFileLoadException A configuration file could not be loaded.
   * @throws DataFileLoadException A csv file could not be loaded.
   */
  private DataStore(File dataFile) throws ConfigurationFileLoadException, DataFileLoadException {
    populateReferenceSets();
    loadColumnDefs();
    loadData(dataFile);
    rebuildRowMap();
  }

  public static DataStore createDataStore()
      throws ConfigurationFileLoadException, DataFileLoadException {
    return createDataStore(null);
  }

  public static DataStore createDataStore(File dataFile)
      throws ConfigurationFileLoadException, DataFileLoadException {
    if (DataStore.datastore_instance == null) {
      DataStore.datastore_instance = new DataStore(dataFile);
    }
    return DataStore.datastore_instance;
  }

  /**
   * Check for presence of filename in DataStore
   *
   * @param filename The file name being searched for
   * @return true if there is a row for filename, false otherwise.
   */
  public boolean has_row(String filename) {
    return rowMap.containsKey(filename);
  }

  /**
   * Setup all the references sets.
   *
   * <p>ToDo: These should probably come from a configuration file.
   */
  private void populateReferenceSets() {
    INTEGER_UNITS = new HashSet<>();
//    addAll(INTEGER_UNITS, );

    TEXT_UNITS = new HashSet<>();
    addAll(TEXT_UNITS, "text", "timestamp", "fractional degrees", "editable text");

    FLOAT_UNITS = new HashSet<>();
    addAll(FLOAT_UNITS, "pixels", "meters", "millimeters", "unitless percentage", "fractional pixels");

    BOOLEAN_UNITS = new HashSet<>();
    addAll(BOOLEAN_UNITS, "boolean");

    EDITABLE = new HashSet<>();
    addAll(EDITABLE, "editable text");

    FETCHABLE_LENGTHS = new HashSet<>();
    addAll(FETCHABLE_LENGTHS, "length", "auto length");

    FETCHABLE_POINTS = new HashSet<>();
    addAll(FETCHABLE_POINTS, "point", "auto point");

    UNIT_CLASSES = new HashMap<>();
    INTEGER_UNITS.forEach(unit -> UNIT_CLASSES.put(unit, Integer.class));
    FLOAT_UNITS.forEach(unit -> UNIT_CLASSES.put(unit, Double.class));
    TEXT_UNITS.forEach(unit -> UNIT_CLASSES.put(unit, String.class));
    BOOLEAN_UNITS.forEach(unit -> UNIT_CLASSES.put(unit, Boolean.class));
  }

  /**
   * This is where we load the column specifications. The only entry that is absolutely needed is
   * "Filename".
   *
   * @throws ConfigurationFileLoadException if a configuration file can't be loaded.
   */
  private void loadColumnDefs() throws ConfigurationFileLoadException {
    ArrayList<HashMap<String, String>> column_config_file =
        ConfigurationLoader.get_csv_file("CSV-Columns.csv");
    columnMap = new String[column_config_file.size()];
    descriptors.clear();
    for (int i = 0; i < column_config_file.size(); i++) {
      HashMap<String, String> row = column_config_file.get(i);
      final String column_name = row.get("column_name");
      descriptors.put(
          column_name,
          new ColumnDescriptor(
              column_name,
              row.get("description"),
              row.get("units"),
              row.get("export"),
              row.get("measurement_type"),
              row.get("editable"),
              row.get("is_metadata"),
              i));
      columnMap[i] = column_name;
    }
  }

  /**
   * Indicates if there is unsaved data in the data store.
   *
   * @return dirty state of datastore
   */
  public boolean dirty() {
    return dataDirty;
  }

  /**
   * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine
   * how many rows it should display. This method should be quick, as it is called frequently during
   * rendering.
   *
   * @return the number of rows in the model
   * @see #getColumnCount
   */
  @Override
  public int getRowCount() {
    return data.size();
  }

  /**
   * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine
   * how many columns it should create and display by default.
   *
   * @return the number of columns in the model
   * @see #getRowCount
   */
  @Override
  public int getColumnCount() {
    return descriptors.size();
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
    final String filename = (String) data.get(rowIndex).get("Filename");
    final String column_name = columnMap[columnIndex];
    return get_value(filename, column_name);
  }

  /**
   * Retrieves a value specified by column for image_filename from the data store. If the value
   * can't be retrieved return missing.
   *
   * @param image_filename The name of the file being annotated.
   * @param column The name of the column being annotated
   * @return The retrieved value from the table
   */
  public Object get_value(String image_filename, String column) {
    return get_value(image_filename, column, null);
  }

  /**
   * Retrieves a value specified by column for image_filename from the data store. If the value
   * can't be retrieved return missing.
   *
   * @param image_filename The name of the file being annotated.
   * @param column The name of the column being annotated
   * @param missing What to return if the value is missing.
   * @return The retrieved value from the table
   */
  public Object get_value(String image_filename, String column, Object missing) {
    final int row = find_row(image_filename);
    if (row == -1) {
      return missing;
    }
    Object value = data.get(row).getOrDefault(column, null);
    if (value == null) {
      return missing;
    }
    return value;
  }

  /**
   * Get the row index for a filename.
   *
   * @param image_filename the image name to look for in the data store.
   * @return Index of row containing image name if found, -1 otherwise.
   */
  public int find_row(String image_filename) {
    return rowMap.getOrDefault(image_filename, -1);
  }

  /**
   * Converts a <code>String</code> representation of a column into the correct type.
   *
   * @param column the column name
   * @param value the <code>String</code> representation
   * @return an instance of the value converted the correct type for column.
   * @throws Exception raised if value can't be converted.
   */
  private Object loadingMapper(String column, String value) throws Exception {
    if (value == null) {
      return null;
    }
    String val = value.trim();
    if (val.equals("NA") || val.equals("")) {
      return null;
    }
    String units = descriptors.get(column).units;
    if (TEXT_UNITS.contains(units)) {
      return val;
    }
    if (FLOAT_UNITS.contains(units)) {
      return new Double(val);
    }
    if (INTEGER_UNITS.contains(units)) {
      if (val.contains(".")) {
        val = val.substring(0, val.indexOf('.'));
      }
      return new Integer(val);
    }
    if (BOOLEAN_UNITS.contains(units)) {
      return new Boolean(val.equals("true"));
    }

    throw new Exception(String.format("Unknown unit specification \"%s\"", units));
  }

  /**
   * Loads the CSV file specified by self.csv_path and self.csv_filename. If error_on_unknown=False
   * and a csv has extra columns not listed in the CSV-Columns.csv config, those columns will be
   * skipped when the csv is loaded into the datastore. If error_on_unknown=True and a csv has extra
   * columns, an error will be thrown and the csv will not be loaded.
   *
   * @param dataFile <code>File</code> referring to a CSV file holding the measurements.
   * @throws DataFileLoadException raised if the there any issues opening dataFile.
   */
  public void loadData(File dataFile) throws DataFileLoadException {
    if (dataFile == null) {
      csvFile = null;
      dataDirty = false;
      data.clear();
      rebuildRowMap();
      return;
    }

    ArrayList<java.util.HashMap<String, String>> records;
    try {
      records = loadCSVAsMap(dataFile.toString());
    } catch (CSVFileLoadException e) {
      throw new DataFileLoadException(String.format("Couldn't load %s", dataFile), e);
    }

    csvFile = dataFile;
    dataDirty = false;
    data.clear();

    Set<String> field_check = new HashSet<>();
    addAll(field_check, columnMap);
    for (final HashMap<String, String> record : records) {
      HashMap<String, Object> processed_row = new HashMap<>();
      for (String key : record.keySet()) {
        if (field_check.contains(key)) {
          String val = record.get(key);
          try {
            processed_row.put(key, loadingMapper(key, val));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      // Eat blank rows
      for (final Object value : processed_row.values()) {
        if (value != null) {
          data.add(processed_row);
          break;
        }
      }
    }
    rebuildRowMap();
    fireTableDataChanged();
  }

  /**
   * Retrieve the name of a row.
   *
   * @param row index of the row
   * @return The name of the file associated with the row, null if the row index is out of bounds.
   */
  public String getRowName(int row) {
    if (row >= data.size() || row < 0) {
      return null;
    }
    return (String) data.get(row).get("Filename");
  }

  /**
   * Returns the name for the column:
   *
   * @param column the column being queried
   * @return a string containing the default name of <code>column</code>
   */
  @Override
  public String getColumnName(int column) {
    if (column < columnMap.length && column >= 0) {
      return columnMap[column];
    }
    return "";
  }

  /**
   * Returns a column given its name. Implementation is naive so this should be overridden if this
   * method is to be called often. This method is not in the <code>TableModel</code> interface and
   * is not used by the <code>JTable</code>.
   *
   * @param columnName string containing name of column to be located
   * @return the column with <code>columnName</code>, or -1 if not found
   */
  @Override
  public int findColumn(String columnName) {
    if (descriptors.containsKey(columnName)) {
      return descriptors.get(columnName).index;
    }
    return -1;
  }

  /**
   * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
   *
   * @param columnIndex the column being queried
   * @return the Object.class
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    final String units = descriptors.get(columnMap[columnIndex]).units;
    if (FLOAT_UNITS.contains(units)) {
      return Double.class;
    }
    if (INTEGER_UNITS.contains(units)) {
      return Integer.class;
    }
    if (TEXT_UNITS.contains(units)) {
      return String.class;
    }
    return super.getColumnClass(columnIndex);
  }

  /**
   * Returns true if a cell's unit is in EDITABLE.
   *
   * @param rowIndex the row being queried
   * @param columnIndex the column being queried
   * @return false
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex >= columnMap.length || columnIndex < 0) {
      return false;
    }
    return EDITABLE.contains(descriptors.get(columnMap[columnIndex]).units);
  }

  /**
   * Wraps a call to insert_value.
   *
   * @param aValue value to assign to cell
   * @param rowIndex row of cell
   * @param columnIndex column of cell
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    String filename = (String) data.get(rowIndex).get("Filename");
    String column_name = columnMap[columnIndex];
    Object old_value = get_value(filename, column_name);
    if (old_value != aValue) {
      insert_value(filename, column_name, aValue);
    }
  }

  /**
   * @param image_filename the image being annotated
   * @param column the column of interest
   * @param value the value to store
   * @throws NoSuchElementException when an illegal column is specified
   */
  public void insert_value(final String image_filename, final String column, Object value)
      throws NoSuchElementException, ClassCastException {
    //    System.err.printf("Inserting %s at %s, %s\n", value, image_filename, column);
    if (!descriptors.containsKey(column)) {
      throw new NoSuchElementException(String.format("%s is not a legal column name", column));
    }

    Class<?> expected = UNIT_CLASSES.get(descriptors.get(column).units);
    if (value != null && !expected.isInstance(value)) {
      throw new ClassCastException(
          String.format("Got %s instead of %s", value.getClass().getName(), expected.getName()));
    }
    int row = find_row(image_filename);
    if (row == -1) { // New record created
      HashMap<String, Object> record = new HashMap<>();
      record.put("Filename", image_filename);
      record.put(column, value);
      data.add(record);
      rebuildRowMap();
      row = rowMap.get(image_filename);
      dataDirty = true;
      fireTableRowsInserted(row, row);
      fireTableCellUpdated(row, descriptors.get("Filename").index);
      fireTableCellUpdated(row, descriptors.get(column).index);
    } else { // Update existing record
      if (!data.get(row).containsKey(column) || !(data.get(row).get(column) == value)) {
        data.get(row).put(column, value);
        dataDirty = true;
        if (column.equals("Filename")) {
          rebuildRowMap();
        }
        fireTableCellUpdated(row, descriptors.get(column).index);
      }
    }
  }

  /** Manages building mapping of filenames to row indices and vice-versa. */
  private void rebuildRowMap() {
    rowMap = new HashMap<>();
    int index = 0;
    for (final String filename : current_files()) {
      rowMap.put(filename, index);
      index += 1;
    }
  }

  /**
   * Returns a list of the files currently referenced by the DataStore
   *
   * @return An array of strings listing the current files.
   */
  public String[] current_files() {
    String[] result = new String[data.size()];
    int index = 0;
    for (final HashMap<String, Object> row : data) {
      result[index] = (String) row.get("Filename");
      index += 1;
    }
    return result;
  }

  /**
   * Delete a row from the data store.
   *
   * @param image_filename The row to be deleted.
   */
  public void remove_row(String image_filename) {
    final int row = find_row(image_filename);
    if (row != -1) {
      data.remove(row);
      rebuildRowMap();
      dataDirty = true;
      fireTableRowsDeleted(row, row);
    }
  }

  /**
   * Retrieve a
   *
   * @param filename the image name.
   * @param point_column the point base name.
   * @return The points for point_column. If the value is not stored return null, else return the
   *     point. If the point_column's measurement_type isn't it FETCHABLE_POINTS, return null.
   */
  public Point get_point(String filename, String point_column) {
    final String x_col = String.format(X_COL, point_column);
    final String y_col = String.format(Y_COL, point_column);
    if (!FETCHABLE_POINTS.contains(descriptors.get(x_col).measurement_type)) {
      return null;
    }
    Integer x_value = get_value(filename, x_col, Integer.class, null);
    Integer y_value = get_value(filename, y_col, Integer.class, null);

    if (x_value != null && y_value != null) {
      return new Point(x_value, y_value);
    }
    return null;
  }

  /**
   * Retrieves a value specified by column for image_filename from the data store. If the value
   * can't be retrieved return missing.
   *
   * @param image_filename The name of the file being annotated.
   * @param column The name of the column being annotated
   * @return The retrieved value from the table
   */
  public <T> T get_value(String image_filename, String column, Class<T> type, Object missing) {
    return type.cast(get_value(image_filename, column, missing));
  }

  /**
   * Stores a new point value.
   *
   * @param filename the file name if the image being annotated.
   * @param point_column The point measurement to be updated
   * @param point The new point value, must not be null.
   */
  public void set_point(String filename, String point_column, Point point) {
    final String x_col = String.format(X_COL, point_column);
    final String y_col = String.format(Y_COL, point_column);
    if (!descriptors.containsKey(x_col)) {
      throw new NoSuchElementException(String.format("%s is not a legal point name", point_column));
    }
    if (!descriptors.get(x_col).measurement_type.equals("point")) {
      return;
    }
    if (point == null) {
      insert_value(filename, x_col, null);
      insert_value(filename, y_col, null);
    } else {
      insert_value(filename, x_col, point.getX());
      insert_value(filename, y_col, point.getY());
    }
  }

  /**
   * Retrieve the endpoints for a length column
   *
   * @param filename the file name if the image being annotated.
   * @param length_column the length column
   * @return An array of <code>Point</code> instances.
   */
  public Point[] getEndpoints(String filename, String length_column) {
    if (!FETCHABLE_LENGTHS.contains(descriptors.get(length_column).measurement_type)) {
      return null;
    }

    final String x_col_start = String.format(X_START_LENGTH, length_column);
    final String y_col_start = String.format(Y_START_LENGTH, length_column);
    final String x_col_end = String.format(X_END_LENGTH, length_column);
    final String y_col_end = String.format(Y_END_LENGTH, length_column);

    Integer x_start = get_value(filename, x_col_start, Integer.class, null);
    Integer y_start = get_value(filename, y_col_start, Integer.class, null);
    Integer x_end = get_value(filename, x_col_end, Integer.class, null);
    Integer y_end = get_value(filename, y_col_end, Integer.class, null);

    if (x_start == null || y_start == null || x_end == null || y_end == null) {
      return null;
    }

    Point[] results = new Point[2];
    results[0] = new Point(x_start, y_start);
    results[1] = new Point(x_end, y_end);
    return results;
  }

  /**
   * Set the endpoints for a length column.
   *
   * <p>If either start or end are null, both are set to null
   *
   * @param filename the file name if the image being annotated.
   * @param length_column the length column
   * @param start starting point for length
   * @param end ending point for length
   */
  public void set_endpoints(String filename, String length_column, Point start, Point end) {
    if (!FETCHABLE_LENGTHS.contains(descriptors.get(length_column).measurement_type)) {
      return;
    }

    final String x_col_start = String.format(X_START_LENGTH, length_column);
    final String y_col_start = String.format(Y_START_LENGTH, length_column);
    final String x_col_end = String.format(X_END_LENGTH, length_column);
    final String y_col_end = String.format(Y_END_LENGTH, length_column);

    if ((start != null) && (end != null)) {
      insert_value(filename, x_col_start, start.getX());
      insert_value(filename, y_col_start, start.getY());
      insert_value(filename, x_col_end, end.getX());
      insert_value(filename, y_col_end, end.getY());
    } else {
      insert_value(filename, x_col_start, null);
      insert_value(filename, y_col_start, null);
      insert_value(filename, x_col_end, null);
      insert_value(filename, y_col_end, null);
    }
  }

  /**
   * Writes a CSV file to the file specified via dataFile. All columns are written.
   *
   * @param dataFile The location to export to.
   * @throws IOException when the data can't be saved to <code>dataFile</code>.
   */
  public void save_as_csv(File dataFile) throws IOException {
    save_as_csv(dataFile, false);
  }

  /**
   * Writes a CSV file to the file specified via dataFile.
   *
   * <p>If export is True strip out any column where the export entry in the CSV-Columns config file
   * is false.
   *
   * @param dataFile The location to export to.
   * @param export If true only exports columns marked for export in the CSV-Columns config.
   * @throws IOException when the data can't be saved to <code>dataFile</code>.
   */
  public void save_as_csv(File dataFile, boolean export) throws IOException {
    ArrayList<String> headers = new ArrayList<>();
    if (export) {
      for (final String header : columnMap) {
        if (descriptors.get(header).export) {
          headers.add(header);
        }
      }
    } else {
      addAll(headers, columnMap);
    }

    FileWriter output = new FileWriter(dataFile);
    CSVWriter csv_writer = new CSVWriter(output);

    String[] output_headers = new String[headers.size()];
    for (int i = 0; i < headers.size(); i++) {
      output_headers[i] = headers.get(i);
    }
    csv_writer.writeNext(output_headers);

    for (HashMap<String, Object> datum : data) {
      csv_writer.writeNext(prepare_row(datum, headers));
    }
    csv_writer.flush();
    csv_writer.close();
    output.close();

    csvFile = dataFile;
    dataDirty = false;
    fireTableDataChanged();
  }

  /**
   * Prepares a row for writing to a CSV file.
   *
   * @param row the row from <code>data</code> being processed.
   * @param exportColumns the columns being exported.
   * @return an array of <code>String</code> containing the values being exported.
   */
  private String[] prepare_row(HashMap<String, Object> row, ArrayList<String> exportColumns) {
    String[] result = new String[exportColumns.size()];
    for (int i = 0; i < result.length; i++) {
      String column_name = exportColumns.get(i);
      result[i] = savingMapper(column_name, row.get(column_name));
    }
    return result;
  }

  /**
   * Generates the representation for saving a column whose value is null
   *
   * @param columnName the column being mapped.
   * @param value the values of the column
   * @return <code>String</code> representation of value.
   */
  private String savingMapper(String columnName, Object value) {
    if (value == null) {
      if (TEXT_UNITS.contains(descriptors.get(columnName).units)) {
        return "";
      } else if (BOOLEAN_UNITS.contains(descriptors.get(columnName).units)) {
        return "false";
      } else {
        return "NA";
      }
    }
    return value.toString();
  }

  /**
   * Get the display name for the current csv file.
   *
   * @return the name component of csvFile if valid.
   */
  public String getCsvFileName() {
    if (csvFile == null) {
      return "No File";
    }
    return csvFile.getName();
  }

  public File getCsvFile() {
    return csvFile;
  }
}
