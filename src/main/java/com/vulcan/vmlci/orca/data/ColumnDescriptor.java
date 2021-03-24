/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.data;

public class ColumnDescriptor {
    public final String name;
    public final String description;
    public final String units;
    public final boolean export;
    public final String measurement_type;
    public final boolean editable;
    public final boolean is_metadata;
    public final int index;

    public ColumnDescriptor(String name, String description, String units, String export, String measurement_type, String editable, String is_metadata, int index) {
        this.name = name;
        this.description = description;
        this.units = units;
        this.export = "true".equalsIgnoreCase(export);
        this.measurement_type = measurement_type;
        this.editable = "true".equalsIgnoreCase(editable);
        this.is_metadata = "true".equalsIgnoreCase(is_metadata);
        this.index = index;
    }
}
