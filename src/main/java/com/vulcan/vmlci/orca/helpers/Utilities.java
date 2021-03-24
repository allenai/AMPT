/*
 *  Copyright (c) 2021 Vulcan Inc.
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

package com.vulcan.vmlci.orca.helpers;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Helper functions. */
public enum Utilities {
  ;

  /**
   * Loads a CSV file into an ArrayList of Maps.
   *
   * @param targetFile The full path to the file.
   * @return The contents of the file.
   * @throws CSVFileLoadException if there are any issues loading the file.
   */
  public static ArrayList<HashMap<String, String>> loadCSVAsMap(String targetFile)
      throws CSVFileLoadException {
    final CSVReader csv_reader;
    final ArrayList<HashMap<String, String>> result = new ArrayList<>();
    try {
      csv_reader = new CSVReader(new FileReader(targetFile));
    } catch (final FileNotFoundException e) {
      throw new CSVFileLoadException("IO problem encountered loading '" + targetFile + "'", e);
    }
    final String[] headers;
    try {
      headers = csv_reader.readNext();
    } catch (final IOException e) {
      throw new CSVFileLoadException("IO problem encountered loading '" + targetFile + "'", e);
    }

    final List<String[]> values;
    try {
      values = csv_reader.readAll();
    } catch (final IOException e) {
      throw new CSVFileLoadException("IO problem encountered loading '" + targetFile + "'", e);
    }

    final int n_cols = headers.length;
    for (final String[] nextLine : values) {
      final HashMap<String, String> record = new HashMap<>();
      for (int i = 0; i < n_cols; i++) {
        record.put(headers[i], nextLine[i]);
      }
      result.add(record);
    }
    return result;
  }
}
