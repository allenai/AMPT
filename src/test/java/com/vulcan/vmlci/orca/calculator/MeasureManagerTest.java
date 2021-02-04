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

import com.vulcan.vmlci.orca.ConfigurationLoader;
import com.vulcan.vmlci.orca.DataStore;
import com.vulcan.vmlci.orca.DataStoreTest;
import junit.framework.TestCase;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class MeasureManagerTest extends TestCase {
  DataStore ds = null;
  Path originalConfigPath = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.originalConfigPath = ConfigurationLoader.getConfigDirectory();
    String testingConfigPath =
        DataStoreTest.class.getResource("/measurement-tool-config/").getPath();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
    this.ds = new DataStore();
  }

  /**
   * Load a known good csv file and verify the length.
   *
   * <p>This test is present just to make sure we're loading data correctly.
   */
  public void testLoad_data_known_good() {
    load_test_data("/data/sample_full.csv");
    assertEquals(80, ds.getRowCount());
  }

  /**
   * Load a CSV file into the <code>DataStore</code>
   *
   * @param test_csv the test case CSV file.
   */
  private void load_test_data(String test_csv) {
    File test_file = new File(DataStoreTest.class.getResource(test_csv).getPath());
    try {
      this.ds.loadData(test_file);
    } catch (com.vulcan.vmlci.orca.DataFileLoadException e) {
      e.printStackTrace();
      fail();
    }
  }

  public void test_update_single_value_changes() {

    try {
      new MeasurementManager(ds);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    ds.insert_value("foo", "SNDF_x_start", 0.);
    ds.insert_value("foo", "SNDF_y_start", 3.);
    ds.insert_value("foo", "SNDF_x_end", 4.);
    ds.insert_value("foo", "SNDF_y_end", 0.);
    assertEquals(5., ds.get_value("foo","SNDF"));
    ds.insert_value("foo", "SNDF_y_end", null);
    assertNull(ds.get_value("foo","SNDF"));
  }
  public void test_update_point_value_changes() {
    BaseCalculator calculator = null;
    try {
      calculator = new MeasurementManager(ds);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    ds.set_point("foo", "SN", new Point2D.Double(0.,3.));
    ds.set_point("foo", "DF", new Point2D.Double(4.,0.));
    assertEquals(5., ds.get_value("foo","SNDF"));
    ds.insert_value("foo", "SNDF_y_end", null);
    assertNull(ds.get_value("foo","SNDF"));
  }

}

