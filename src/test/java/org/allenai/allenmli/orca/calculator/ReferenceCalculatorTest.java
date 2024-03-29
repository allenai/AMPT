/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
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

package org.allenai.allenmli.orca.calculator;

import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.DataStoreTest;
import org.allenai.allenmli.orca.data.Point;
import org.allenai.allenmli.orca.helpers.ConfigurationFileLoadException;
import org.allenai.allenmli.orca.helpers.ConfigurationLoader;
import org.allenai.allenmli.orca.helpers.DataFileLoadException;
import junit.framework.TestCase;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Paths.get(DataStoreTest.class.getResource("/measurement-tool-config/").toURI()).toString();
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
    assertTrue("Preflight failed", referenceCalculator.preflight_measurement("Line Refs", "foo"));
    HashMap<String, Point[]> result;
    result = (HashMap<String, Point[]>) referenceCalculator.do_measurement("Line Refs", "foo");
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
    assertTrue("Preflight failed", referenceCalculator.preflight_measurement("Line Refs", "foo"));
    HashMap<String, Point[]> result;
    result = (HashMap<String, Point[]>) referenceCalculator.do_measurement("Line Refs", "foo");
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
