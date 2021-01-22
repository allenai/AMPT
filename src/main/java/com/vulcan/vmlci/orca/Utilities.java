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

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utilities {

    public static ArrayList<HashMap<String, String>> loadCSVAsMap(String targetFile) throws FileLoadException {
        CSVReader csv_reader;
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        try {
            csv_reader = new CSVReader(new FileReader(targetFile));
        } catch (FileNotFoundException e) {
            throw new FileLoadException(String.format("IO problem encountered loading '%s'", targetFile), e);
        }
        String[] headers = new String[0];
        try {
            headers = csv_reader.readNext();
        } catch (IOException e) {
            throw new FileLoadException(String.format("IO problem encountered loading '%s'", targetFile), e);
        } catch (CsvValidationException e) {
            throw new FileLoadException(String.format("Malformed CSV header in '%s'", targetFile), e);
        }
        List<String[]> values = null;
        try {
            values = csv_reader.readAll();
        } catch (IOException e) {
            throw new FileLoadException(String.format("IO problem encountered loading '%s'", targetFile), e);
        } catch (CsvException e) {
            throw new FileLoadException(String.format("Malformed CSV row in '%s'", targetFile), e);
        }

        final int n_cols = headers.length;
        for (final String[] nextLine : values) {
            HashMap<String, String> record = new HashMap<>();
            for (int i = 0; i < n_cols; i++) {
                record.put(headers[i], nextLine[i]);
            }
            result.add(record);
        }
        return result;
    }
}
