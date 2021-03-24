"""Augment the CSV-Columns file to generate length measurement."""

#   Copyright (c) 2021 Vulcan Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from __future__ import print_function

import csv
import os
from collections import OrderedDict

import time


def main():
    """Augment the ./CSV-Columns.csv file"""
    records = OrderedDict()
    with open("./CSV-Columns.csv") as infile:
        dict_reader = csv.DictReader(infile)
        headers = dict_reader.fieldnames
        for row in dict_reader:
            records[row['column_name']] = row

    # Add start and end columns for length and auto length columns
    for column in [x for x in records.keys() if
                   records[x]['measurement_type'] in ('length', 'auto length')]:
        for label_augment, desc_augment in [("{}_x_start", "Starting X for {}"),
                                            ("{}_y_start", "Starting y for {}"),
                                            ("{}_x_end", "Ending X for {}"),
                                            ("{}_y_end", "Ending y for {}")]:
            target_col = label_augment.format(column)
            if target_col not in records:
                desc = desc_augment.format(records[column]['description'])
                records[target_col] = {'column_name': target_col, 'description': desc,
                                       'units': 'pixels', 'measurement_type': 'auto point',
                                       'export': 'False', 'editable': 'False', 'is_metadata': 'False'}
    for column in [x for x in records.keys() if
                   records[x]['measurement_type'] in ('point', 'length')]:
        if records[column]['measurement_type'] == 'point' and column.endswith("_y"):
            continue
        measurement_type = records[column]['measurement_type']
        if column.endswith("_x"):
            column = column[:-2]
        desc = "Review of {} {}".format(column, measurement_type)
        target_col = "{}_reviewed".format(column)
        records[target_col] = {'column_name': target_col, 'description': desc,
                               'units': 'boolean', 'measurement_type': 'boolean',
                               'export': 'False', 'editable': 'False', 'is_metadata': 'False'}

    os.rename("./CSV-Columns.csv", "./CSV-Columns.csv.{}.bak".format(int(time.time())))
    with open("./CSV-Columns.csv", 'w') as outfile:
        # with sys.stdout as outfile:
        dict_writer = csv.DictWriter(outfile, headers)
        dict_writer.writeheader()
        dict_writer.writerows(records.values())


if __name__ == '__main__':
    main()
