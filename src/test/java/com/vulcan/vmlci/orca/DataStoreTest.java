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

public class DataStoreTest extends TestCase {
    DataStore ds = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
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
    }

    public void testGetColumnCount() {
    }

    public void testGetValueAt() {
    }
}