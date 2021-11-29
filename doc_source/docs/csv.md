---
title: CSV Manipulation
summary: Managing the CSV data file
authors:
    - Jenna James
    - Jordan Buckley
    - Paul Albee
date: 2021-04-23
---
## Main CSV File
All data flows in and out of AMPT via a CSV file. The source CSV file must have at a minimum a column named 'Filename' which corresponds to the image filenames. Loading a a CSV file to start working with AMPT is not required as AMPT can create a correctly formatted file.

## Loading a CSV file

![Load CSV](img/load_csv.png)

1. At the bottom of the AMPT Plugin panel, click Load CSV button.
1. Navigate to your desired CSV on your machine.
1. Click Open. You now have a working CSV

Any columns that are not specified in the [CSV-Columns.csv configuration file](csv_columns_config.md) will be ignored, and will not be written by the [Save CSV](#save-csv) or [Export CSV](#export-csv) operations.

## View CSV

![View CSV](img/view_csv.png)

The View CSV button opens a window that displays the currently active data.

## Save CSV

![Save CSV](img/save_csv.png)

The Save CSV button presents a dialog to allow the user to save the current data. The data saved by this option contains all the endpoints identified by user, as well as other metadata needed for AMPT's operation. This data supports the rendering of the measurements within AMPT, editing of previously recorded measurements, and may be used by other software. The exact columns that will be written are specified in the [CSV-Columns.csv configuration file](csv_columns_config.md).

## Export CSV


![Export CSV](img/export_csv.png)

The Export CSV button presents a dialog to allow the user to save an extract of the current data. The data saved by this option contains only those columns identified as exportable in the [CSV-Columns.csv configuration file](csv_columns_config.md). This data supports the rendering of the measurements within AMPT, editing of previously recorded measurements, and may be used by other software.

