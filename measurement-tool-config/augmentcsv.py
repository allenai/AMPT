"""Augment the CSV-Columns file to generate length measurement."""

# Copyright Vulcan Inc. 2020

# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:

# 1. Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.

# 2. Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.

# 3. Neither the name of the copyright holder nor the names of its
# contributors may be used to endorse or promote products derived from
# this software without specific prior written permission.

# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from __future__ import print_function

import csv
import sys
import os
import time
from collections import OrderedDict


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
        # for label_augment, desc_augment in [("{}_x_start", "Starting X for {}"),
        #                                     ("{}_y_start", "Starting y for {}"),
        #                                     ("{}_x_end", "Ending X for {}"),
        #                                     ("{}_y_end", "Ending y for {}")]:
        #     target_col = label_augment.format(column)
        #     if target_col not in records:
        #         desc = desc_augment.format(records[column]['description'])
        #         records[target_col] = {'column_name': target_col, 'description': desc,
        #                                'units': 'pixels', 'measurement_type': 'auto point',
        #                                'export': 'False'}
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
