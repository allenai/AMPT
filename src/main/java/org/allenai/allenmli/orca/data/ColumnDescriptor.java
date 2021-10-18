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

package org.allenai.allenmli.orca.data;

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
