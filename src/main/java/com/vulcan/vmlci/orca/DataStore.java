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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class DataStore extends AbstractTableModel {

    // Helper sets.
    public HashSet<String> INTEGER_UNITS;
    public HashSet<String> TEXT_UNITS;
    public HashSet<String> FLOAT_UNITS;
    public HashSet<String> EDITABLE;
    public HashSet<String> FETCHABLE_LENGTHS;
    public HashSet<String> FETCHABLE_POINTS;

    private String csv_path = null;
    private String csv_filename = null;
    private final HashMap<String, ColumnDescriptor> descriptors = new HashMap<>();
    private HashMap<String, Integer> __row_map = null;
    private String[] __column_map = null;
    private final ArrayList<HashMap<String, Object>> data = new ArrayList<>();
    private final boolean __dirty = false;

    public DataStore() throws ConfigLoadException {
        this(null, null);
    }

    public DataStore(String csv_path, String csv_filename) throws ConfigLoadException {

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
        }
    }

    private void load_data(String csv_path, String csv_filename) {
    }

    private void populate_reference_sets() {
        this.INTEGER_UNITS = new HashSet<>();
        Collections.addAll(INTEGER_UNITS, "pixels");

        TEXT_UNITS = new HashSet<>();
        Collections.addAll(TEXT_UNITS, "text", "timestamp", "fractional degrees", "editable text");

        FLOAT_UNITS = new HashSet<>();
        Collections.addAll(FLOAT_UNITS, "meters", "millimeters", "unitless percentage", "fractional pixels");

        EDITABLE = new HashSet<>();
        Collections.addAll(EDITABLE, "editable text");

        FETCHABLE_LENGTHS = new HashSet<>();
        Collections.addAll(FETCHABLE_LENGTHS, "length", "auto length");

        FETCHABLE_POINTS = new HashSet<>();
        Collections.addAll(FETCHABLE_POINTS, "point", "auto point");
    }

    private void __load_column_defs() throws ConfigLoadException {
        ArrayList<HashMap<String, String>> column_config_file = ConfigurationLoader.get_csv_file("CSV-Columns.csv");
        this.__column_map = new String[column_config_file.size()];
        this.descriptors.clear();
        for (int i = 0; i < column_config_file.size(); i++) {
            HashMap<String, String> row = column_config_file.get(i);
            final String column_name = row.get("column_name");
            this.descriptors.put(column_name,
                    new ColumnDescriptor(row.get("description"), row.get("units"), row.get("export"), row.get("measurement_type"),
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
        /*
            def getValueAt(self, row, column):  # pylint: disable=invalid-name
        """Access the element at row, column"""
        filename = self.data[row]['Filename']
        column_name = self.__column_map[column]
        return self.get_value(filename, column_name)

         */
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
/*
    # Private utilities
    def __saving_mapper(self, key, value):
        """Maps the value for column key appropriately if value is None"""
        if value is None:
            if self.descriptors[key].units in TEXT_UNITS:
                return ''
            return 'NA'
        return value

    def __loading_mapper(self, key, val):
        """Map the value associated with key to the correct type."""
        val = val.strip() # removes leading and trailing whitepace
        if val in ('NA', ''):
            return None

        units = self.descriptors[key].units
        if units in TEXT_UNITS:
            return val
        elif units in FLOAT_UNITS:
            return float(val)
        elif units in INTEGER_UNITS:
            return int(val)
        raise ValueError('Unknown unit specification "{}"'.format(units))

    def load_data(self, csv_path=None, csv_file=None, error_on_unknown=False):
        """Loads the CSV file specified by self.csv_path and self.csv_filename.
           If error_on_unknown=False and a csv has extra columns not listed in
           the CSV-Columns.csv config, those columns will be skipped when the
           csv is loaded into the datastore. If error_on_unknown=True and a csv
           has extra columns, an error will be thrown and the csv will not be
           loaded.
        """
        self.csv_path = csv_path
        self.csv_filename = csv_file
        self.dirty = False
        if self.data:
            self.data = list()
            self.__rebuild_row_map()
        if self.csv_path is None or self.csv_filename is None:
            return
        self.data = list()
        fullname = os.path.join(self.csv_path, self.csv_filename)
        with open(fullname) as csv_file_handle:
            reader = csv.DictReader(csv_file_handle)
            for row in reader:
                processed_row = {}
                for key, val in row.items():
                    if key in self.__column_map:
                        processed_row.update({key: self.__loading_mapper(key, val)})
                    elif error_on_unknown:
                        raise ValueError('Unknown column specification "{}"'.format(key))
                # lines 121-127 remove empty rows in csv files before loading
                has_data = False
                for key, val in processed_row.items():
                    if processed_row[key] is not None:
                        has_data = True
                if has_data:
                    self.data.append(processed_row)
        self.__rebuild_row_map()
        self.fireTableDataChanged()




    def getRowName(self, row):  # pylint: disable=invalid-name
        """Returns the row name"""
        if row >= len(self.data):
            return None
        return self.data[row]['Filename']

    # AbstractTableModel Methods
    def getColumnName(self, column):  # pylint: disable=invalid-name
        """Returns the column name"""
        if column >= len(self.__column_map):
            return None
        return self.__column_map[column]

    def findColumn(self, columnName):  # pylint: disable=invalid-name
        """Retrieve the column name"""
        if columnName in self.descriptors:
            return self.descriptors[columnName].index
        return -1

    def getColumnClass(self, columnIndex):  # pylint: disable=invalid-name
        """Returns the class of the column"""
        units = self.descriptors[self.__column_map[columnIndex]].units
        if units in TEXT_UNITS:
            return str
        elif units in FLOAT_UNITS:
            return float
        elif units in INTEGER_UNITS:
            return int
        return super(DataStore, self).getColumnClass(columnIndex)

    def isCellEditable(self, rowIndex, columnIndex):  # pylint: disable=invalid-name,unused-argument
        """Returns True if a cell's type is in EDITABLE"""
        if columnIndex >= len(self.__column_map):
            return False
        return self.descriptors[self.__column_map[columnIndex]].units in EDITABLE

    def setValueAt(self, aValue, rowIndex, columnIndex):  # pylint: disable=invalid-name
        """Setting the value of a cell"""
        filename = self.data[rowIndex]['Filename']
        column_name = self.__column_map[columnIndex]
        self.insert_value(filename, column_name, aValue)


    # descriptors
    def __getitem__(self, item):
        """Retrieves a row from the """
        row = self.find_row(item)
        if row == -1:
            raise KeyError(item)
        return self.data[row]

    def __setitem__(self, key, value):
        """Places a dictionary into data, assuming it has an appropriate Filename and
        all all keys are allowed by the CSV-Columns configuration file."""
        if not isinstance(value, dict):
            raise TypeError("Please use the insert_value method to insert a single value")
        if key != value.get('Filename', None):
            raise ValueError(
                'RHS must be a dictionary where the "Filename" entry must match the key')
        allowed_keys = set(self.descriptors.keys())
        proposed_keys = set(value.keys())
        illegal_keys = proposed_keys - allowed_keys
        if illegal_keys:
            raise ValueError(
                '({}) not valid column names'.format(", ".join(list(illegal_keys))))
        row = self.find_row(key)
        if row == -1:
            self.data.append(value)
            self.__rebuild_row_map()
            row = self.__row_map[key]
            self.dirty = True
            self.fireTableRowsInserted(row, row)
        else:
            self.data[row] = value
            self.dirty = True
            self.fireTableRowsUpdated(row, row)

    def __delitem__(self, v):
        self.remove_row(v)

    def __iter__(self):
        return iter(self.data)

    def __len__(self):
        """Allows use of the len built-in"""
        return len(self.data)

    # Public API




    def insert_value(self, image_filename, column, value):
        """Inserts a column value for image_filename into the data store."""
        if column not in self.descriptors.keys():
            raise IndexError('{} is not a legal column name'.format(column))
        row = self.find_row(image_filename)
        if row == -1:
            self.data.append({'Filename': image_filename, column: value})
            self.__rebuild_row_map()
            row = self.__row_map[image_filename]
            self.dirty = True
            self.fireTableRowsInserted(row, row)
            self.fireTableCellUpdated(row, self.descriptors['Filename'].index)
            self.fireTableCellUpdated(row, self.descriptors[column].index)
        else:
            if column not in self.data[row] or self.data[row][column] != value:
                self.data[row][column] = value
                self.dirty = True
                self.fireTableCellUpdated(row, self.descriptors[column].index)

    def get_value(self, image_filename, column, missing=None, cast_to=None):
        """Retrieves a value specified by column for image_filename from the data store.
        If the value can't be retrieve return the missing value

        @param image_filename: The name of the file being annotated.
        @param column:  The name of the column being annotated
        @param missing: What to return if the value is missing.
        @param cast_to: How to cast the return value. Shold by a type or callable.
        @return: The value from the table
        """
        row = self.find_row(image_filename)
        if row == -1:
            return missing
        value = self.data[row].get(column, None)
        if value is None:
            return missing
        elif cast_to:
            value = cast_to(value)
        return value

    def get_double_value(self, image_filename, column, missing=float('nan')):
        """Retrieves a double value specified by column for image_filename from the data store.
        If the value can't be retrieve return the missing value
        """
        warnings.warn("get_double_value will be removed in the future, please use get_value",
                      DeprecationWarning, stacklevel=2)
        return float(self.get_value(image_filename, column, missing=missing))

    def get_string_value(self, image_filename, column, missing=''):
        """Retrieves a string value specified by column for image_filename from the data store.
        If the value can't be retrieve return the missing value
        """
        warnings.warn("get_string_value will be removed in the future, please use get_value",
                      DeprecationWarning, stacklevel=2)
        return str(self.get_value(image_filename, column, missing=missing))

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

    def remove_row(self, image_filename):
        """Delete a row from the data store."""
        row = self.find_row(image_filename)
        if row != -1:
            del self.data[row]
            self.__rebuild_row_map()
            self.dirty = True
            self.fireTableRowsDeleted(row, row)

    def get_endpoints(self, filename, length_column):
        """Returns the endpoints for a length measurement as tuple,
         if they are defined. If the endpoints have not been saved, the tuple contains Nones.

        If the endpoints are not defined, return None
        """
        if self.descriptors[length_column].measurement_type not in FETCHABLE_LENGTHS:
            return None

        endpoints = list()
        for label_augment in ["{}_x_start", "{}_y_start", "{}_x_end", "{}_y_end"]:
            target_col = label_augment.format(length_column)
            endpoints.append(self.get_value(filename, target_col, missing=None, cast_to=int))
        return tuple(endpoints)

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

    def get_point(self, filename, point_column):
        """Returns the point
         if they are defined. If the endpoints have not been saved, the tuple contains Nones.

        If the endpoints are not defined, return None
        """
        x_col = "{}_x".format(point_column)
        y_col = "{}_y".format(point_column)
        if self.descriptors[x_col].measurement_type not in FETCHABLE_POINTS:
            return None
        x_value, y_value = (self.get_value(filename, x_col, missing=None, cast_to=int),
                            self.get_value(filename, y_col, missing=None, cast_to=int))
        if x_value and y_value:
            return x_value, y_value
        return None

    def set_point(self, filename, point_column, point):
        """Stores the endpoints for a length measurement,
        """
        x_col = "{}_x".format(point_column)
        y_col = "{}_y".format(point_column)
        if self.descriptors[x_col].measurement_type != 'point':
            return
        if point is None:
            point = (None, None)
        for column, value in zip([x_col, y_col], point):
            self.insert_value(filename, column, value)
        return

 */
}
