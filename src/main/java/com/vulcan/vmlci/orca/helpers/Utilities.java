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
