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

package com.vulcan.vmlci.orca.calculator;

import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.data.DataStoreTest;
import com.vulcan.vmlci.orca.helpers.DataFileLoadException;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("SameParameterValue")
public class BaseCalculatorTest extends TestCase {
  DataStore ds = null;
  Path originalConfigPath = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.originalConfigPath = ConfigurationLoader.getConfigDirectory();
    String testingConfigPath =
        Paths.get(DataStoreTest.class.getResource("/measurement-tool-config/").toURI()).toString();
    ConfigurationLoader.setConfigDirectory(testingConfigPath);
    this.ds = DataStore.createDataStore();
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

  public void test_length() {
    BaseCalculator calculator = null;
    try {
      calculator = new BaseCalculatorTestingAdapter(ds);
    } catch (FileNotFoundException | ConfigurationFileLoadException e) {
      TestCase.fail(e.getMessage());
    }
    TestCase.assertEquals(
        "Got a non-zero length from zero length vector",
        0.,
        BaseCalculatorTestingAdapter.length(0., 0., 0., 0.));
    ds.insert_value("foo", "SNDF_x_start", 0.);
    ds.insert_value("foo", "SNDF_y_start", 3.);
    ds.insert_value("foo", "SNDF_x_end", 4.);
    ds.insert_value("foo", "SNDF_y_end", 0.);
    TestCase.assertEquals(5., calculator.do_measurement("SNDF", "foo"));
  }

  public void test_bad_measurement() {
    BaseCalculator mm = null;
    try {
      mm = new BaseCalculatorTestingAdapter(ds);
    } catch (FileNotFoundException | ConfigurationFileLoadException e) {
      TestCase.fail(e.getMessage());
    }
    ds.insert_value("foo", "SNDF_x_start", 0.);
    try {
      mm.do_measurement("BOGUS", "foo");
      TestCase.fail();
    } catch (IllegalArgumentException e) {
      TestCase.assertTrue(e.getMessage().contains("BOGUS"));
    }
  }

  public void test_bad_function() {
    BaseCalculator mm = null;
    try {
      mm = new BaseCalculatorTestingAdapter(ds);
    } catch (FileNotFoundException | ConfigurationFileLoadException e) {
      TestCase.fail(e.getMessage());
    }
    ds.insert_value("foo", "SNDF_x_start", 0.);
    try {
      mm.do_measurement("BAD_FUNC", "foo");
      TestCase.fail();
    } catch (IllegalArgumentException e) {
      TestCase.assertTrue(e.getMessage().contains("BAD_FUNC"));
    }
  }

  private static class BaseCalculatorTestingAdapter extends BaseCalculator {

    public BaseCalculatorTestingAdapter(DataStore ds) throws FileNotFoundException, ConfigurationFileLoadException {
      super(ds);
    }

    @Override
    protected String getConfigurationFile() {
      return "BadMeasurementConf.json";
    }
  }
}
