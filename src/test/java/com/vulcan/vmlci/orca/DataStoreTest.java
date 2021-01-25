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

import junit.framework.TestCase;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class DataStoreTest extends TestCase {
    final String[] SAMPLE_SHORT_FILES = {"2018-09-03 21-40-22.jpg",
            "2018-09-03 21-40-23.jpg",
            "2018-09-03 21-40-38.jpg"
    };
    DataStore ds = null;
    Path originalConfigPath = null;

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ConfigurationLoader.setConfigDirectory(originalConfigPath);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.originalConfigPath = ConfigurationLoader.getConfigDirectory();
        String testingConfigPath = DataStoreTest.class.getResource("/measurement-tool-config/").getPath();
        ConfigurationLoader.setConfigDirectory(testingConfigPath);
        this.ds = new DataStore();
    }

    public void testINTEGER_UNITS() {
        assertEquals("Failure - set cardinality for INTEGER_UNITS incorrect",
                1, ds.INTEGER_UNITS.size());
    }

    public void testTEXT_UNITS() {
        assertEquals("Failure - set cardinality for TEXT_UNITS incorrect",
                4, ds.TEXT_UNITS.size());
    }

    public void testFLOAT_UNITS() {
        assertEquals("Failure - set cardinality for FLOAT_UNITS incorrect",
                4, ds.FLOAT_UNITS.size());
    }

    public void testEDITABLE() {
        assertEquals("Failure - set cardinality for EDITABLE incorrect",
                1, ds.EDITABLE.size());
    }

    public void testFETCHABLE_LENGTHS() {
        assertEquals("Failure - set cardinality for FETCHABLE_LENGTHS incorrect",
                2, ds.FETCHABLE_LENGTHS.size());
    }

    public void testFETCHABLE_POINTS() {
        assertEquals("Failure - set cardinality for FETCHABLE_POINTS incorrect",
                2, ds.FETCHABLE_POINTS.size());
    }

    public void testGetRowCount() {
        assertEquals(0, this.ds.getRowCount());
    }

    public void testGetColCount() {
        assertEquals(0, this.ds.getRowCount());
    }

    public void testFind_row() {
        final String test_csv = "/data/test_blankrow.csv";
        load_test_data(test_csv);
        assertEquals(2, ds.getRowCount());
        assertEquals(0, ds.find_row("mammal1.jpg"));
        assertEquals(1, ds.find_row("mammal2.jpg"));
    }

    private void load_test_data(String test_csv) {
        Path test_file = Paths.get(DataStoreTest.class.getResource(test_csv).getPath());
        try {
            this.ds.load_data(test_file.getParent().toString(), test_file.getFileName().toString());
        } catch (FileLoadException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testLoad_data_known_good() {
        load_test_data("/data/sample_full.csv");
        assertEquals(80, ds.getRowCount());
    }

    public void testLoad_data_blank_row() {
        load_test_data("/data/test_blankrow.csv");
        assertEquals(2, ds.getRowCount());
    }

    public void testGetColumnClass() {
        assertEquals(String.class, ds.getColumnClass(0));
        assertEquals(Double.class, ds.getColumnClass(6));
    }

    public void testGetColumnName() {
        assertEquals("Filename", ds.getColumnName(0));
        assertEquals("", ds.getColumnName(1000));
        assertEquals("HEAD", ds.getColumnName(17));
    }

    public void testFindColumn() {
        assertEquals(0, ds.findColumn("Filename"));
        assertEquals(-1, ds.findColumn("1000"));
        assertEquals(17, ds.findColumn("HEAD"));
    }

    public void testIsCellEditable() {
        assertTrue(ds.isCellEditable(0, 13));
        assertFalse(ds.isCellEditable(0, 0));
    }


    public void test_insert_value_new() {
        // Check that we can insert a value into a new row.
        this.ds.insert_value("newrow", "Position", 42);
        int retrieved_value = (Integer) this.ds.get_value("newrow", "Position");
        assertEquals(42, retrieved_value);
    }

    /**
     * Check that we can insert a value into an existing row.
     */
    public void test_insert_value_existing() {
        this.load_test_data("/data/sample_short.csv");
        String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
        assertEquals("4TB", retrieved_value);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "Position", "3LR");
        retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
        assertEquals("3LR", retrieved_value);
    }

    /**
     * Check that we can insert a value into an existing row.
     */
    public void test_insert_value_dirty_check_no_change() {
        this.load_test_data("/data/sample_short.csv");
        assertFalse(this.ds.dirty());
        String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "Position", retrieved_value);
        assertFalse(this.ds.dirty());
    }

    public void test_insert_value_dirty_check_change() {
        this.load_test_data("/data/sample_short.csv");
        assertFalse(this.ds.dirty());
        String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "Position", String.format("BLAB%s", retrieved_value));
        assertTrue(this.ds.dirty());
    }

    public void test_insert_value_dirty_check_new() {
        assertFalse(this.ds.dirty());
        this.ds.insert_value("newrow", "Position", 42);
        this.ds.get_value("newrow", "Position");
        assertTrue(this.ds.dirty());
    }

    public void testSetValueAt() {
        this.load_test_data("/data/sample_short.csv");
        this.ds.setValueAt("Foo", 0, 3);
        String retrieved_value = (String) this.ds.get_value(SAMPLE_SHORT_FILES[0], "Position");
        assertEquals("Foo", retrieved_value);
    }

    public void testRemove_row() {
        this.load_test_data("/data/sample_short.csv");
        assertTrue(this.ds.has_row(SAMPLE_SHORT_FILES[0]));
        this.ds.remove_row(SAMPLE_SHORT_FILES[0]);
        assertFalse(this.ds.has_row(SAMPLE_SHORT_FILES[0]));
    }

    public void testGet_point_not_present() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull(result);
    }

    public void testGet_point_y_half_not_present() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_x", 10.);
        result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull(result);
    }

    public void testGet_point_x_half_not_present() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_y", 10.);
        result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull(result);
    }

    public void testGet_point() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_x", -10.);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SN_y", 10.);
        result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNotNull(result);
        Point2D ground_truth = new Point2D.Double(-10., 10.);
        assertEquals(ground_truth, result);
    }

    public void testSet_point() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.set_point(SAMPLE_SHORT_FILES[0], "SN", new Point2D.Double(-10., 10.));
        assertEquals(-10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_x"));
        assertEquals(10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_y"));
    }

    public void testSet_point_null() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.set_point(SAMPLE_SHORT_FILES[0], "SN", null);
        assertNull(this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_x"));
        assertNull(this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_y"));
    }

    public void testClear_point() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);
        this.ds.set_point(SAMPLE_SHORT_FILES[0], "SN", new Point2D.Double(-10., 10.));
        assertEquals(-10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_x"));
        assertEquals(10., this.ds.get_value(SAMPLE_SHORT_FILES[0], "SN_y"));
        this.ds.clear_point(SAMPLE_SHORT_FILES[0], "SN");
        result = this.ds.get_point(SAMPLE_SHORT_FILES[0], "SN");
        assertNull("this.ds should not contain any points at this time", result);

    }

    public void testGet_endpoints_not_present() {
        this.load_test_data("/data/sample_short.csv");
        Object result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNull(result);
    }

    public void testGet_endpoints_partials() {
        this.load_test_data("/data/sample_short.csv");
        Point2D.Double[] result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNull(result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_x_start", 1.);
        result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNull(result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_y_start", 2.);
        result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNull(result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_x_end", 3.);
        result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNull(result);
        this.ds.insert_value(SAMPLE_SHORT_FILES[0], "SNDF_y_end", 4.);
        result = this.ds.get_endpoints(SAMPLE_SHORT_FILES[0], "SNDF");
        assertNotNull(result);
        assertEquals(new Point2D.Double(1., 2.), result[0]);
        assertEquals(new Point2D.Double(3., 4.), result[1]);
        assertEquals(2, result.length);
    }

    public void testSave_as_csv() {
        Date when = new Date();
        File scratch_file = new File(String.format("/tmp/test_dump_%d.csv",when.getTime()));
        this.load_test_data("/data/sample_full.csv");
        try {
            this.ds.save_as_csv(scratch_file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        DataStore reloaded = null;
        try {
            reloaded = new DataStore(scratch_file.getParent(), scratch_file.getName());
        } catch (FileLoadException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(reloaded);
        int orig_rows = this.ds.getRowCount();
        int orig_cols = this.ds.getColumnCount();

        assertEquals(orig_rows, reloaded.getRowCount());
        assertEquals(orig_cols, reloaded.getColumnCount());
        for (int row = 0; row < orig_rows; row++) {
            for (int col = 0; col < orig_cols; col++) {
                final Object orig = this.ds.getValueAt(row, col);
                final Object reload = reloaded.getValueAt(row, col);
                assertEquals(orig, reload);
            }
        }
        scratch_file.deleteOnExit();
    }
}