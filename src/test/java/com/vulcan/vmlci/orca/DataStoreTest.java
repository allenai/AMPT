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

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataStoreTest extends TestCase {
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
        Path test_file = Paths.get(DataStoreTest.class.getResource("/test_blankrow.csv").getPath());
        try {
            this.ds.load_data(test_file.getParent().toString(), test_file.getFileName().toString());
        } catch (FileLoadException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(2, ds.getRowCount());
        assertEquals(0, ds.find_row("mammal1.jpg"));
        assertEquals(1, ds.find_row("mammal2.jpg"));
    }

    public void testLoad_data_known_good() {
//        Path test_file = Paths.get(DataStoreTest.class.getResource("/sample_full.csv").getPath());
        Path test_file = Paths.get(DataStoreTest.class.getResource("/sample_full.csv").getPath());
        try {
            this.ds.load_data(test_file.getParent().toString(), test_file.getFileName().toString());
        } catch (FileLoadException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(80, ds.getRowCount());
    }

    public void testLoad_data_blank_row() {
        Path test_file = Paths.get(DataStoreTest.class.getResource("/test_blankrow.csv").getPath());
        try {
            this.ds.load_data(test_file.getParent().toString(), test_file.getFileName().toString());
        } catch (FileLoadException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(2, ds.getRowCount());
    }

    public void testGetColumnClass() {
        assertEquals(String.class, ds.getColumnClass(0));
        assertEquals(Float.class, ds.getColumnClass(6));
    }

    public void testCurrent_files() {
    }

    public void testHas_row() {
    }

    public void testDirty() {
    }

    public void testGetColumnCount() {
    }

    public void testGetValueAt() {
    }

    public void testGet_value() {
    }

    public void testTestGet_value() {
    }

    public void testLoad_data() {
    }

    public void testGetRowName() {

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
        assertTrue(ds.isCellEditable(0,13));
        assertFalse(ds.isCellEditable(0,0));
    }
}