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

import javax.swing.table.AbstractTableModel;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Collections.addAll;


public class DataStore extends AbstractTableModel {

    public static final String X_COL = "%s_x";
    public static final String Y_COL = "%s_y";
    public static final String X_START_LENGTH = "%s_x_start";
    public static final String Y_START_LENGTH = "%s_y_start";
    public static final String X_END_LENGTH = "%s_x_end";
    public static final String Y_END_LENGTH = "%s_y_end";
    private final HashMap<String, ColumnDescriptor> descriptors = new HashMap<>();
    private final ArrayList<HashMap<String, Object>> data = new ArrayList<>();
    // Helper sets.
    public HashSet<String> INTEGER_UNITS;
    public HashSet<String> TEXT_UNITS;
    public HashSet<String> FLOAT_UNITS;
    public HashSet<String> EDITABLE;
    public HashSet<String> FETCHABLE_LENGTHS;
    public HashSet<String> FETCHABLE_POINTS;
    private String csv_path = null;
    private String csv_filename = null;
    private HashMap<String, Integer> __row_map = null;
    private String[] __column_map = null;
    private boolean __dirty = false;

    public DataStore() throws FileLoadException {
        this(null, null);
    }

    public DataStore(String csv_path, String csv_filename) throws FileLoadException {

        this.populate_reference_sets();
        this.csv_path = csv_path;
        this.csv_filename = csv_filename;
        this.__load_column_defs();
        this.load_data(csv_path, csv_filename);
        this.__rebuild_row_map();
    }

    /**
     * Returns a list of the files currently referenced by the DataStore
     *
     * @return An array of strings listing the current files.
     */
    public String[] current_files() {
        String[] result = new String[this.data.size()];
        int index = 0;
        for (final HashMap<String, Object> row : this.data) {
            result[index] = (String) row.get("Filename");
            index += 1;
        }
        return result;
    }

    /**
     * Check for presence of image_filename in DataStore
     *
     * @param image_filename The file name being searched for
     * @return true if there is a row for image_filename, false otherwise.
     */
    public boolean has_row(String image_filename) {
        return this.__row_map.containsKey(image_filename);
    }

    /**
     * Manages building mapping of filenames to row indices and vice-versa.
     */
    private void __rebuild_row_map() {
        this.__row_map = new HashMap<>();
        int index = 0;
        for (final String filename : this.current_files()) {
            this.__row_map.put(filename, index);
            index += 1;
        }
    }

    private void populate_reference_sets() {
        this.INTEGER_UNITS = new HashSet<>();
        addAll(INTEGER_UNITS, "pixels");

        TEXT_UNITS = new HashSet<>();
        addAll(TEXT_UNITS, "text", "timestamp", "fractional degrees", "editable text");

        FLOAT_UNITS = new HashSet<>();
        addAll(FLOAT_UNITS, "meters", "millimeters", "unitless percentage", "fractional pixels");

        EDITABLE = new HashSet<>();
        addAll(EDITABLE, "editable text");

        FETCHABLE_LENGTHS = new HashSet<>();
        addAll(FETCHABLE_LENGTHS, "length", "auto length");

        FETCHABLE_POINTS = new HashSet<>();
        addAll(FETCHABLE_POINTS, "point", "auto point");
    }

    private void __load_column_defs() throws FileLoadException {
        ArrayList<HashMap<String, String>> column_config_file = ConfigurationLoader.get_csv_file("CSV-Columns.csv");
        this.__column_map = new String[column_config_file.size()];
        this.descriptors.clear();
        for (int i = 0; i < column_config_file.size(); i++) {
            HashMap<String, String> row = column_config_file.get(i);
            final String column_name = row.get("column_name");
            this.descriptors.put(column_name,
                    new ColumnDescriptor(column_name, row.get("description"), row.get("units"), row.get("export"), row.get("measurement_type"),
                            row.get("editable"), row.get("is_metadata"), i));
            this.__column_map[i] = column_name;
        }
    }

    public boolean dirty() {
        return __dirty;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    @Override
    public int getRowCount() {
        return this.data.size();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    @Override
    public int getColumnCount() {
        return this.descriptors.size();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final String filename = (String) this.data.get(rowIndex).get("Filename");
        final String column_name = this.__column_map[columnIndex];
        return this.get_value(filename, column_name);
    }

    public Object get_value(String image_filename, String column) {
        return get_value(image_filename, column, null);
    }

    /**
     * Retrieves a value specified by column for image_filename from the data store.
     * If the value can't be retrieved return missing.
     *
     * @param image_filename The name of the file being annotated.
     * @param column         The name of the column being annotated
     * @param missing        What to return if the value is missing.
     * @return The retrieved value from the table
     */
    public Object get_value(String image_filename, String column, Object missing) {
        final int row = this.find_row(image_filename);
        if (row == -1) {
            return missing;
        }
        Object value = this.data.get(row).getOrDefault(column, null);
        if (value == null) {
            return missing;
        }
        return value;
    }


    /**
     * Get the row index for a filename.
     *
     * @return Index of row containing image name if found, -1 otherwise.
     */
    public int find_row(String image_filename) {
        return this.__row_map.getOrDefault(image_filename, -1);
    }

    private String __saving_mapper(String key, Object value) {
        if (value == null) {
            if (TEXT_UNITS.contains(this.descriptors.get(key).units)) {
                return "";
            } else {
                return "NA";
            }
        }
        return value.toString();
    }

    private Object __loading_mapper(String key, String value) throws Exception {
        if (value == null) {
            return null;
        }
        String val = value.trim();
        if (val.equals("NA") || val.equals("")) {
            return null;
        }
        String units = this.descriptors.get(key).units;
        if (TEXT_UNITS.contains(units)) {
            return val;
        }
        if (FLOAT_UNITS.contains(units)) {
            return new Double(val);
        }
        if (INTEGER_UNITS.contains(units)) {
            return new Integer(val);
        }
        throw new Exception(String.format("Unknown unit specification \"%s\"", units));
    }


    /**
     * Loads the CSV file specified by self.csv_path and self.csv_filename.
     * If error_on_unknown=False and a csv has extra columns not listed in
     * the CSV-Columns.csv config, those columns will be skipped when the
     * csv is loaded into the datastore. If error_on_unknown=True and a csv
     * has extra columns, an error will be thrown and the csv will not be
     * loaded.
     */
    public void load_data(String csv_path, String csv_filename) throws FileLoadException {
        this.csv_path = csv_path;
        this.csv_filename = csv_filename;
        this.__dirty = false;
        this.data.clear();
        this.__rebuild_row_map();
        if (this.csv_path == null || this.csv_filename == null) {
            return;
        }
        Path file_path = Paths.get(this.csv_path, this.csv_filename);

        ArrayList<HashMap<String, String>> records = Utilities.loadCSVAsMap(file_path.toString());
        Set<String> field_check = new HashSet<>();
        addAll(field_check, this.__column_map);
        for (final HashMap<String, String> record : records) {
            HashMap<String, Object> processed_row = new HashMap<>();
            for (String key : record.keySet()) {
                if (field_check.contains(key)) {
                    String val = record.get(key);
                    try {
                        processed_row.put(key, this.__loading_mapper(key, val));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.printf("%s not in set of expected column names\n", key);
                }
            }
            // Eat blank rows
            for (final Object value : processed_row.values()) {
                if (value != null) {
                    this.data.add(processed_row);
                    break;
                }
            }
        }
        this.__rebuild_row_map();
        this.fireTableDataChanged();
    }


    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    public Class<?> getColumnClass(int columnIndex) {
        final String units = this.descriptors.get(this.__column_map[columnIndex]).units;
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
     * Retrieve the name of a row.
     *
     * @param row index of the row
     * @return The name of the file associated with the row, null if the row index is out of bounds.
     */
    public String getRowName(int row) {
        if (row >= this.data.size() || row < 0) {
            return null;
        }
        return (String) this.data.get(row).get("Filename");
    }

    /**
     * Returns the name for the column:
     *
     * @param column the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    @Override
    public String getColumnName(int column) {
        if (column < this.__column_map.length && column >= 0) {
            return this.__column_map[column];
        }
        return "";
    }

    /**
     * Returns a column given its name.
     * Implementation is naive so this should be overridden if
     * this method is to be called often. This method is not
     * in the <code>TableModel</code> interface and is not used by the
     * <code>JTable</code>.
     *
     * @param columnName string containing name of column to be located
     * @return the column with <code>columnName</code>, or -1 if not found
     */
    @Override
    public int findColumn(String columnName) {
        if (this.descriptors.containsKey(columnName)) {
            return this.descriptors.get(columnName).index;
        }
        return -1;
    }

    /**
     * Returns true if a a cell's unit is in EDITABLE.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return false
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex >= this.__column_map.length || columnIndex < 0) {
            return false;
        }
        return this.EDITABLE.contains(this.descriptors.get(this.__column_map[columnIndex]).units);
    }

    /**
     * Wraps a call to insert_value.
     *
     * @param aValue      value to assign to cell
     * @param rowIndex    row of cell
     * @param columnIndex column of cell
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String filename = (String) this.data.get(rowIndex).get("Filename");
        String column_name = this.__column_map[columnIndex];
        this.insert_value(filename, column_name, aValue);
    }


    public void insert_value(final String image_filename, final String column, Object value) throws NoSuchElementException {
        if (!this.descriptors.containsKey(column)) {
            throw new NoSuchElementException(String.format("%s is not a legal column name", column));
        }
        int row = this.find_row(image_filename);
        if (row == -1) { // New record created
            HashMap<String, Object> record = new HashMap<>();
            record.put("Filename", image_filename);
            record.put(column, value);
            this.data.add(record);
            this.__rebuild_row_map();
            row = this.__row_map.get(image_filename);
            this.__dirty = true;
            this.fireTableRowsInserted(row, row);
            this.fireTableCellUpdated(row, this.descriptors.get("Filename").index);
            this.fireTableCellUpdated(row, this.descriptors.get(column).index);
        } else { // Update existing record
            if (!this.data.get(row).containsKey(column) || !this.data.get(row).get(column).equals(value)) {
                this.data.get(row).put(column, value);
                this.__dirty = true;
                this.fireTableCellUpdated(row, this.descriptors.get(column).index);
            }
        }
    }

    /**
     * Delete a row from the data store.
     *
     * @param image_filename The row to be deleted.
     */
    public void remove_row(String image_filename) {
        final int row = this.find_row(image_filename);
        if (row != -1) {
            this.data.remove(row);
            this.__rebuild_row_map();
            this.__dirty = true;
            this.fireTableRowsDeleted(row, row);
        }
    }

    /**
     * Writes a CSV file to the file specified via export_path.
     * All columns are written.
     *
     * @param export_path The location to export to.
     */
    public void save_as_csv(String export_path) {
        save_as_csv(export_path, false);
    }

    /**
     * Writes a CSV file to the file specified via export_path.
     * <p>
     * If export is True strip out any column where the export entry in the CSV-Columns config
     * file is false.
     *
     * @param export_path The location to export to.
     * @param export      If true only exports columns marked for export in the CSV-Columns config.
     */
    public void save_as_csv(String export_path, boolean export) {
        ArrayList<String> headers = new ArrayList<>();
        if (export) {
            for (final String header : this.__column_map) {
                if (this.descriptors.get(header).export) {
                    headers.add(header);
                }
            }
        } else {
            addAll(headers, this.__column_map);
        }
    }


    /**
     * Retrieve a
     *
     * @param filename     the image name.
     * @param point_column the point base name.
     * @return The points for point_column. If the value is not stored return null, else return the point. If the
     * point_column's measurement_type isn't it FETCHABLE_POINTS, return null.
     */
    public Point2D.Double get_point(String filename, String point_column) {
        final String x_col = String.format(X_COL, point_column);
        final String y_col = String.format(Y_COL, point_column);
        if (!FETCHABLE_POINTS.contains(this.descriptors.get(x_col).measurement_type)) {
            return null;
        }
        Object x_value = this.get_value(filename, x_col);
        Object y_value = this.get_value(filename, y_col);

        if (x_value != null && y_value != null) {
            return new Point2D.Double((Double) x_value, (Double) y_value);
        }
        return null;
    }

    /**
     * @param filename the image name.
     * @param point_column
     * @param point
     */
    public void set_point(String filename, String point_column, Point2D.Double point) {
        final String x_col = String.format(X_COL, point_column);
        final String y_col = String.format(Y_COL, point_column);
        if (!this.descriptors.containsKey(x_col)) {
            throw new NoSuchElementException(String.format("%s is not a legal point name", point_column));
        }
        if (!this.descriptors.get(x_col).measurement_type.equals("point") || point == null) {
            return;
        }
        this.insert_value(filename, x_col, point.getX());
        this.insert_value(filename, y_col, point.getY());
    }

    /**
     * @param filename the image name.
     * @param point_column
     */
    public void clear_point(String filename, String point_column) {
        final String x_col = String.format(X_COL, point_column);
        final String y_col = String.format(Y_COL, point_column);
        if (!this.descriptors.containsKey(x_col)) {
            throw new NoSuchElementException(String.format("%s is not a legal point name", point_column));
        }
        if (!this.descriptors.get(x_col).measurement_type.equals("point")) {
            return;
        }
        this.insert_value(filename, x_col, null);
        this.insert_value(filename, y_col, null);


    }

    /**
     * @param filename
     * @param length_column
     * @return
     */
    public Point2D.Double[] get_endpoints(String filename, String length_column) {
        if (!FETCHABLE_LENGTHS.contains(this.descriptors.get(length_column).measurement_type)) {
            return null;
        }

        final String x_col_start = String.format(X_START_LENGTH, length_column);
        final String y_col_start = String.format(Y_START_LENGTH, length_column);
        final String x_col_end = String.format(X_END_LENGTH, length_column);
        final String y_col_end = String.format(Y_END_LENGTH, length_column);

        Double x_start = (Double) this.get_value(filename, x_col_start);
        Double y_start = (Double) this.get_value(filename, y_col_start);
        Double x_end = (Double) this.get_value(filename, x_col_end);
        Double y_end = (Double) this.get_value(filename, y_col_end);

        if (x_start == null || y_start == null || x_end == null || y_end == null) {
            return null;
        }

        Point2D.Double[] results = new Point2D.Double[2];
        results[0] = new Point2D.Double(x_start, y_start);
        results[1] = new Point2D.Double(x_end, y_end);
        return results;
    }

/*
    def set_endpoints(self, filename, length_column, endpoints):
        """Stores the endpoints for a length measurement,
        """
        if self.descriptors[length_column].measurement_type != 'length':
            return
        if endpoints is None:
            endpoints = (None, None, None, None)
        for label_augment, value in zip(["{}_x_start", "{}_y_start", "{}_x_end", "{}_y_end"],
                                        endpoints):
            target_col = label_augment.format(length_column)
            self.insert_value(filename, target_col, value)
        return

    # Public API

    def save_as_csv(self, export_path, export=False):
        """Writes a CSV file to the file specified via export_path.
        If export is True strip out any column where the export entry in the CSV-Columns config
        file is false.
        """
        if export:
            headers = [key for key in self.descriptors.keys() if self.descriptors[key].export]
        else:
            headers = [key for key in self.descriptors.keys()]
        with open(export_path, 'wb') as csv_out:
            writer = csv.DictWriter(csv_out, fieldnames=headers, extrasaction='ignore')
            writer.writeheader()
            for row in self.data:
                writer.writerow(
                    {key: self.__saving_mapper(key, row.get(key, None)) for key in headers})
        self.csv_path, self.csv_filename = os.path.split(export_path)
        self.dirty = False
        self.fireTableDataChanged()



 */
}
