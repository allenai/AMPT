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

package com.vulcan.vmlci.orca.data;

import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.helpers.DataFileLoadException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class DataStoreTest extends TestCase {
  final String[] SAMPLE_SHORT_FILES = {
    "2018-09-03 21-40-22.jpg", "2018-09-03 21-40-23.jpg", "2018-09-03 21-40-38.jpg"
  };
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
    this.ds.loadData(null);
  }

  /**
   * Tears down the fixture, for example, close a network connection. This method is called after a
   * test is executed.
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    ConfigurationLoader.setConfigDirectory(originalConfigPath);
  }

  public void testINTEGER_UNITS() {
    TestCase.assertEquals(
        "Failure - set cardinality for INTEGER_UNITS incorrect", 0, ds.INTEGER_UNITS.size());
  }

  public void testTEXT_UNITS() {
    TestCase.assertEquals(
        "Failure - set cardinality for TEXT_UNITS incorrect", 4, ds.TEXT_UNITS.size());
  }

  public void testFLOAT_UNITS() {
    TestCase.assertEquals(
        "Failure - set cardinality for FLOAT_UNITS incorrect", 5, ds.FLOAT_UNITS.size());
  }

  public void testEDITABLE() {
    TestCase.assertEquals(
        "Failure - set cardinality for EDITABLE incorrect", 1, ds.EDITABLE.size());
  }

  public void testFETCHABLE_LENGTHS() {
    TestCase.assertEquals(
        "Failure - set cardinality for FETCHABLE_LENGTHS incorrect",
        2,
        ds.FETCHABLE_LENGTHS.size());
  }

  public void testFETCHABLE_POINTS() {
    TestCase.assertEquals(
        "Failure - set cardinality for FETCHABLE_POINTS incorrect", 2, ds.FETCHABLE_POINTS.size());
  }

  public void testFind_row() {
    final String test_csv = "/data/test_blankrow.csv";
    load_test_data(test_csv);
    TestCase.assertEquals(2, ds.getRowCount());
    TestCase.assertEquals(0, ds.find_row("mammal1.jpg"));
    TestCase.assertEquals(1, ds.find_row("mammal2.jpg"));
  }

  private void load_test_data(String test_csv) {
    File test_file = new File(DataStoreTest.class.getResource(test_csv).getPath());
    try {
      this.ds.loadData(test_file);
    } catch (DataFileLoadException e) {
      e.printStackTrace();
      TestCase.fail();
    }
  }

  public void testLoad_data_known_good() {
    load_test_data("/data/sample_full.csv");
    TestCase.assertEquals(80, ds.getRowCount());
  }

  public void testLoad_data_blank_row() {
    load_test_data("/data/test_blankrow.csv");
    TestCase.assertEquals(2, ds.getRowCount());
  }

  public void testGetColumnClass() {
    TestCase.assertEquals(String.class, ds.getColumnClass(0));
    TestCase.assertEquals(Double.class, ds.getColumnClass(6));
  }

  public void testGetColumnName() {
    TestCase.assertEquals("Filename", ds.getColumnName(0));
    TestCase.assertEquals("", ds.getColumnName(1000));
    TestCase.assertEquals("HEAD", ds.getColumnName(17));
  }

  public void testFindColumn() {
    TestCase.assertEquals(0, ds.findColumn("Filename"));
    TestCase.assertEquals(-1, ds.findColumn("1000"));
    TestCase.assertEquals(17, ds.findColumn("HEAD"));
  }

  public void testIsCellEditable() {
    TestCase.assertTrue(ds.isCellEditable(0, 13));
    TestCase.assertFalse(ds.isCellEditable(0, 0));
  }

  public void test_insert_value_new() {
    // Check that we can insert a value into a new row.
    this.ds.insert_value("newrow", "Position", "42");
    String retrieved_value = (String) this.ds.get_value("newrow", "Position");
    TestCase.assertEquals("42", retrieved_value);
  }

  /** Check that we can insert a value into an existing row. */
  public void test_insert_value_existing() {
    this.load_test_data("/data/sample_short.csv");
    String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
    TestCase.assertEquals("4TB", retrieved_value);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "Position", "3LR");
    retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
    TestCase.assertEquals("3LR", retrieved_value);
  }

  /** Check that we can insert a value into an existing row. */
  public void test_insert_value_dirty_check_no_change() {
    this.load_test_data("/data/sample_short.csv");
    TestCase.assertFalse(this.ds.dirty());
    String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "Position", retrieved_value);
    TestCase.assertFalse(this.ds.dirty());
  }

  public void test_insert_value_dirty_check_change() {
    this.load_test_data("/data/sample_short.csv");
    TestCase.assertFalse(this.ds.dirty());
    String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
    this.ds.insert_value(
        SAMPLE_SHORT_FILES[0], "Position", String.format("BLAB%s", retrieved_value));
    TestCase.assertTrue(this.ds.dirty());
  }

  public void test_insert_value_dirty_check_new() {
    TestCase.assertFalse(this.ds.dirty());
    this.ds.insert_value("newrow", "Position", "42");
    this.ds.get_value("newrow", "Position");
    TestCase.assertTrue(this.ds.dirty());
  }

  public void testSetValueAt() {
    this.load_test_data("/data/sample_short.csv");
    this.ds.setValueAt("Foo", 0, 3);
    String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
    TestCase.assertEquals("Foo", retrieved_value);
  }

  public void testRemove_row() {
    this.load_test_data("/data/sample_short.csv");
    TestCase.assertTrue(this.ds.has_row(SAMPLE_SHORT_FILES[0]));
    this.ds.remove_row(SAMPLE_SHORT_FILES[0]);
    TestCase.assertFalse(this.ds.has_row(SAMPLE_SHORT_FILES[0]));
  }

  public void testGet_point_not_present() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull(result);
  }

  public void testGet_point_y_half_not_present() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull("this.ds should not contain any points at this time", result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_x", 10.);
    result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull(result);
  }

  public void testGet_point_x_half_not_present() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull("this.ds should not contain any points at this time", result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_y", 10.);
    result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull(result);
  }

  public void testGet_point() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull("this.ds should not contain any points at this time", result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_x", -10.);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_y", 10.);
    result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNotNull(result);
    Point ground_truth = new Point(-10., 10.);
    TestCase.assertEquals(ground_truth, result);
  }

  public void testSet_point() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull("this.ds should not contain any points at this time", result);
    this.ds.set_point(SAMPLE_SHORT_FILES[0], "SN", new Point(-10., 10.));
    TestCase.assertEquals(-10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_x"));
    TestCase.assertEquals(10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_y"));
  }

  public void testSet_point_null() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
    TestCase.assertNull("this.ds should not contain any points at this time", result);
    this.ds.set_point(SAMPLE_SHORT_FILES[0], "SN", null);
    TestCase.assertNull(this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_x"));
    TestCase.assertNull(this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_y"));
  }

  public void testGet_endpoints_not_present() {
    this.load_test_data("/data/sample_short.csv");
    Object result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNull(result);
  }

  public void testInsertIncorrectType() {
    this.load_test_data("/data/sample_short.csv");
    try {
      this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_x_start", 1);
      TestCase.fail();
    } catch (ClassCastException e) {
      // This is what we expect.
      return;
    } catch (Exception e) {
      TestCase.fail();
    }
  }

  public void testGet_endpoints_partials() {
    this.load_test_data("/data/sample_short.csv");
    Point[] result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNull(result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_x_start", 1.);
    result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNull(result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_y_start", 2.);
    result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNull(result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_x_end", 3.);
    result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNull(result);
    this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_y_end", 4.);
    result = this.ds.getEndpoints(SAMPLE_SHORT_FILES[0], "SNDF");
    TestCase.assertNotNull(result);
    TestCase.assertEquals(new Point(1., 2.), result[0]);
    TestCase.assertEquals(new Point(3., 4.), result[1]);
    TestCase.assertEquals(2, result.length);
  }

  public void testSave_as_csv() throws Exception {
    File scratch_file = File.createTempFile("test_dump", "csv");
    scratch_file.deleteOnExit();
    this.load_test_data("/data/sample_full.csv");
    try {
      this.ds.save_as_csv(scratch_file);
    } catch (IOException e) {
      e.printStackTrace();
      TestCase.fail();
    }
    DataStore reloaded = this.ds;
    try {
      reloaded.loadData(scratch_file);
    } catch (DataFileLoadException e) {
      e.printStackTrace();
      TestCase.fail();
    }
//    } catch (com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException
//        | com.vulcan.vmlci.orca.helpers.DataFileLoadException e) {
//      e.printStackTrace();
//      fail();
//    }
    TestCase.assertNotNull(reloaded);
    int orig_rows = this.ds.getRowCount();
    int orig_cols = this.ds.getColumnCount();

    TestCase.assertEquals(orig_rows, reloaded.getRowCount());
    TestCase.assertEquals(orig_cols, reloaded.getColumnCount());
    for (int row = 0; row < orig_rows; row++) {
      for (int col = 0; col < orig_cols; col++) {
        final Object orig = this.ds.getValueAt(row, col);
        final Object reload = reloaded.getValueAt(row, col);
        TestCase.assertEquals(orig, reload);
      }
    }
    scratch_file.deleteOnExit();
  }

//  public void testLoadDataMalformed() {
//    File test_file = new File(DataStoreTest.class.getResource("/data/not_a_csv.txt").getPath());
//    try {
//      this.ds.loadData(test_file);
//      TestCase.fail();
//    } catch (DataFileLoadException e) {
//      TestCase.assertTrue(true);
//    }
//  }

  public void testGet_value_generic() {
    ds.set_point("Foo", "SN", new Point(1, 2));
  }
}
