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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class MeasurementManagerTest extends TestCase {
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

  public void testLoad_data_known_good() {
    load_test_data("/data/sample_full.csv");
    assertEquals(80, ds.getRowCount());
  }

  private void load_test_data(String test_csv) {
    File test_file = new File(DataStoreTest.class.getResource(test_csv).getPath());
    try {
      this.ds.loadData(test_file);
    } catch (com.vulcan.vmlci.orca.DataFileLoadException e) {
      e.printStackTrace();
      fail();
    }
  }
  /*
     def test_length(self):
       """Verify that automatic length computations occur."""
       self.assertEqual(MeasurementManager.length(0, 0, 0, 0), 0.0,
                        "Got a non-zero length from zero length vector")
       self.data_store.insert_value('foo', "BH_x", 0)
       self.data_store.insert_value('foo', "BH_y", 3)
       self.data_store.insert_value('foo', "DF_x", 4)
       self.data_store.insert_value('foo', "DF_y", 0)
       updated = int(self.data_store.get_value("foo", "BHDF", missing=None))
       self.assertEqual(updated, 5, "BHDF didn't get computed")

  */

  public void test_length() {
    MeasurementManager mm = null;
    try {
     mm = new MeasurementManager(ds);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }
    assertEquals(
        "Got a non-zero length from zero length vector",
        0.,
        MeasurementManager.length(0., 0., 0., 0.));
    ds.insert_value("foo", "SNDF_x_start", 0.);
    ds.insert_value("foo", "SNDF_y_start", 3.);
    ds.insert_value("foo", "SNDF_x_end", 4.);
    ds.insert_value("foo", "SNDF_y_end", 0.);
    assertEquals(5., mm.do_measurement("SNDF", "foo"));
  }
}
