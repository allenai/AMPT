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

import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.data.DataStoreTest;
import com.vulcan.vmlci.orca.data.Point;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.helpers.DataFileLoadException;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;

@SuppressWarnings("UnusedAssignment")
public class ReferenceCalculatorTest extends TestCase {
  DataStore ds = null;
  Path originalConfigPath = null;
  ReferenceCalculator referenceCalculator = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    originalConfigPath = ConfigurationLoader.getConfigDirectory();
    String testingConfigPath =
        DataStoreTest.class.getResource("/measurement-tool-config/").getPath();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
    ds = DataStore.createDataStore();
    new MeasurementManager(ds);
    referenceCalculator = new ReferenceCalculator(ds);
  }

  /**
   * Load a known good csv file and verify the length.
   *
   * <p>This test is present just to make sure we're loading data correctly.
   */
  public void testLoad_data_known_good() {
    load_test_data("/data/sample_full.csv");
    TestCase.assertEquals(80, ds.getRowCount());
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
    } catch (DataFileLoadException e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }

  public void test_compute_offset_reference_normal() {
    ds.set_point("foo", "SN", new Point(100, 928));
    ds.set_point("foo", "DF", new Point(101, 551));
    ds.set_point("foo", "REFLINE TOP", new Point(52, 865));
    ds.set_point("foo", "REFLINE BOTTOM", new Point(39, 772));
    TestCase.assertTrue("Preflight failed", referenceCalculator.preflight_measurement("Eye Refs", "foo"));
    HashMap<String, Point[]> result;
    result = (HashMap<String, Point[]>) referenceCalculator.do_measurement("Eye Refs", "foo");
    TestCase.assertNotNull(result);
    // Retrieve the 75% reference endpoints
    Point[] meas_endpoints = result.get(String.format("%.0f%% measurement", 0.75 * 100.));

    TestCase.assertEquals(100., (meas_endpoints[0].x + meas_endpoints[1].x) / 2.0, 0.5);
    TestCase.assertEquals(795., (meas_endpoints[0].y + meas_endpoints[1].y) / 2.0, 0.5);
  }

  public void test_compute_offset_reference_reversed_axis() {
    ds.set_point("foo", "DF", new Point(100, 928));
    ds.set_point("foo", "SN", new Point(101, 551));
    ds.set_point("foo", "REFLINE TOP", new Point(52, 865));
    ds.set_point("foo", "REFLINE BOTTOM", new Point(39, 772));
    TestCase.assertTrue("Preflight failed", referenceCalculator.preflight_measurement("Eye Refs", "foo"));
    HashMap<String, Point[]> result;
    result = (HashMap<String, Point[]>) referenceCalculator.do_measurement("Eye Refs", "foo");
    TestCase.assertNotNull(result);
    // Retrieve the 75% reference endpoints
    Point[] meas_endpoints = result.get(String.format("%d%% measurement", 75));

    TestCase.assertEquals(100., ((meas_endpoints[0].x + meas_endpoints[1].x) / 2.0), 0.5);
    TestCase.assertEquals(795., ((meas_endpoints[0].y + meas_endpoints[1].y) / 2.0), 0.5);
  }

  public void test_update_point_value_changes() {
    BaseCalculator calculator = null;
    try {
      calculator = new MeasurementManager(ds);
    } catch (ConfigurationFileLoadException e) {
      TestCase.fail(e.getMessage());
    }
    ds.set_point("foo", "SN", new Point(0, 3));
    ds.set_point("foo", "DF", new Point(4, 0));
    TestCase.assertEquals(5., ds.get_value("foo", "SNDF"));
    ds.insert_value("foo", "SNDF_y_end", null);
    TestCase.assertNull(ds.get_value("foo", "SNDF"));
  }
}
