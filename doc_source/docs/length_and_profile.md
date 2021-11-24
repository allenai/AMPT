---
title: Length Measurements & Body Profiles Panels
summary: Length measurements and body profiles panel element discussions
authors:
    - Jenna James
    - Jordan Buckley
    - Paul Albee
date: 2021-05-14
---
## Length Measurements &amp; Body Profiles Panels
![Length Measurements Panel](img/length_measurements_panel.png)

The Length Measurements and Body Profiles panels are used to aggregate different length measurements, and optionally render them on the image of the animal. The render control is used to turn on length or profile display, while the checkboxes in the lists of measurements control which lines are displayed on the image. Checking the Render box will autmatically disable the input cueing display. The Select All and Clear All buttons select or de-select all measurements for display. The Measurement column shows the name of the measurement, which is defined in the the [CSV-Columns.csv](csv_columns_config.md#default-csv-columnscsv) file. The value is the measured length in fractional pxils. The status column indicates if a measurement has been review. Measurements that show "N/A" for their review status are automatically calculated, all other status values are based in user input, see [Reference Points Details](input.md#reference-points-details) for more information.

The Length Measurements panel draws its values from the [columns](csv_columns_config.md#default-csv-columnscsv) whose [types](csv_columns_config.md#measurement-type) are specified as `length`, and do not contain a `%` in thier name while the Body Profiles values come from `length` columns whos names contain a `%`.
	