---
title: Cue and Calculation Configuration
summary: Discussion on how to configure the cueing display and automatic calculations.
authors:
    - Paul Albee
    - Patrick Johnson
date: 2021-08-03
---

## Calculation Configuration

### Measurement Configuration

The `MeasurementConfg.json` configuration file defines the calculation and parameters for a measurement.

The format of this configuration is as follows:

```
[
  {
    "target": "SNDF",
    "parameters": [
      "SNDF_x_start",
      "SNDF_y_start",
      "SNDF_x_end",
      "SNDF_y_end"
    ],
    "function": "length"
  },
  {
    "target": "SNDF_x_start",
    "parameters": [
      "SN_x"
    ],
    "function": "copy"
  },
  {
    "target": "SNDF_y_start",
    "parameters": [
      "SN_y"
    ],
    "function": "copy"
  },
  {
    "target": "SNDF_x_end",
    "parameters": [
      "DF_x"
    ],
    "function": "copy"
  },
  {
    "target": "SNDF_y_end",
    "parameters": [
      "DF_y"
    ],
    "function": "copy"
  }
]
```

The calculation for each target is performed using a list of parameters and exactly one calculation function. For example, `SNDF` is calculated using the function `length` with parameters `SNDF_x_start`, `SNDF_y_start`, `SNDF_x_end`, `SNDF_y_end`. The function name must always correspond to a function in `MeasurementManager` (or a super class) with the correct number and type of parameters. In this case, the `length` function computes the length from `(x_start, y_start)` to `(x_end, y_end)`. See below for a list of available functions.

Measurements may be defined in terms of other calculated measurements or direct input from the user. For example, `SNDF` is calculated in part from `SNDF_x_start`, which in turn is copied from `SN_x`, a point set by the user. All measurement targets must correspond to a column name defined in the [Column Configuration](csv_columns_config.md).

### Available Functions

| Function           | Parameters                             | Description                                                        |
| ------------------ | -------------------------------------- | ------------------------------------------------------------------ |
| `length`           | `x_start`, `y_start`, `x_end`, `y_end` | Computes the length from `(x_start, y_start)` to `(x_end, y_end)`. |
| `copy`             | `source`                               | Returns `source`.                                                  |
| `ratio`            | `antecedent`, `consequent`             | Computes the ratio of `antecedent:consequent`.                     |
| `parametric_point` | `start`, `end`, `distance`             | Compute the distance along a 1D parametric function.               |

## Cue Configuration

The `CueConfig.json` configuration file maps the measurement being performed to associated cue types. It defines which cues are drawn when a measurement is selected, if the user has enabled cue rendering in the AMPT UI.

The format of this configuration is as follows:

```
{
  "format_version": 1,
  "configuration": [
    {
      "cue": "Head Width",
      "measurements": [
        "HEAD"
      ]
    },
    {
      "cue": "BH",
      "measurements": [
        "BH",
        "SN",
        "DF",
        "FL"
      ]
    },
    {
      "cue": "SN",
      "measurements": [
        "BH",
        "SN",
        "DF",
        "FL"
      ]
    },
    {
      "cue": "DF",
      "measurements": [
        "BH",
        "SN",
        "DF",
        "FL"
      ]
    },
    {
      "cue": "FL",
      "measurements": [
        "BH",
        "SN",
        "DF",
        "FL",
        "FW"
      ]
    }
  ]
```

For example, selecting the `HEAD` measurement will render the `Head Width` cue. This refers to a corresponding measurement in `ReferenceConf.json` that will be displayed:

```
  {
    "target": "Head Width",
    "parameters": [
      "BHDF_x_start",
      "BHDF_y_start",
      "BHDF_x_end",
      "BHDF_y_end",
      15,
      15,
      10
    ],
    "function": "interval_reference_markers"
  },
```

### Reference Configuration

The `ReferenceConf.json` file works in a similar way to `MeasurementConf.json`, except that it defines the calculation and parameters for a cue type. See [Cue Configuration](#cue-configuration) for more information on cues. The format and functionality of `ReferenceConf.json` matches that of `MeasurementConf.json` with the extension of several additional available functions.

The format of this configuration file is as follows:

```
[
  {
    "target": "Body Profile",
    "parameters": [
      "SNDF_x_start",
      "SNDF_y_start",
      "SNDF_x_end",
      "SNDF_y_end",
      5,
      100,
      5
    ],
    "function": "interval_reference_markers"
  },
  {
    "target": "Body Profile 225",
    "parameters": [
      "DFFL_x_start",
      "DFFL_y_start",
      "DFFL_x_end",
      "DFFL_y_end",
      "SNDF_x_start",
      "SNDF_y_start",
      "SNDF_x_end",
      "SNDF_y_end",
      5,
      125,
      5,
      100
    ],
    "function": "interval_reference_markers_with_base_length"
  },
  {
    "target": "Head Width",
    "parameters": [
      "BHDF_x_start",
      "BHDF_y_start",
      "BHDF_x_end",
      "BHDF_y_end",
      15,
      15,
      10
    ],
    "function": "interval_reference_markers"
  },
  {
    "target": "BH",
    "parameters": [
      "\"BH\"",
      "BH_x",
      "BH_y"
    ],
    "function": "render_landmark"
  }
]
```

### Additional Functions

All of the above functions are available plus the following.

| Function                                      | Parameters                                                                                                                                                                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `interval_reference_markers`                  | `axis_x_start`, `axis_y_start`, `axis_x_end`, `axis_y_end`, `start_percentage`, `end_percentage`, `step_size`                                                                         | Draws a reference line with percentage markers along the line.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `interval_reference_markers_with_base_length` | `axis_x_start`, `axis_y_start`, `axis_x_end`, `axis_y_end`, `ref_x_start`, `ref_y_start`, `ref_x_end`, `ref_y_end`, `start_percentage`, `end_percentage`, `step_size`, `label_offset` | Draws a reference line with percentage markers along the line. The marker spacing may be derived from the length of a different line.                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `draw_ref_along_line`                         | `ref_line_x_start`, `ref_line_y_start`, `ref_line_x_end`, `ref_line_y_end`                                                                                                            | Draws a line from `(ref_line_x_start, ref_line_y_start)` to `(ref_line_x_end, ref_line_y_end)` and a marker at offset percentage along the line. The marker gets a label of the form `"%d%%".format(offset)`.                                                                                                                                                                                                                                                                                                                                                                                                    |
| `compute_offset_reference_markers`            | `axis_x_start`, `axis_y_start`, `axis_x_end`, `axis_y_end`, `ref_line_x_start`, `ref_line_y_start`, `ref_line_x_end`, `ref_line_y_end`, `offset`                                      | Computes a set of offset reference markers along an axis. The axis is defined by the line segment from `(axis_x_start, axis_y_start)` to `(axis_x_end, axis_y_end)`. Two line segments orthogonal to the axis line segment intersect the points `(ref_line_x_start, ref_line_y_start)` and `(ref_line_x_end, ref_line_y_end)`. A third line segment orthogonal to the axis intersects the point that is offset percentage along the line segment defined from `(ref_line_x_start, ref_line_y_start)` to `(ref_line_x_end, ref_line_y_end)`. The lengths of the reference lines are 5% of the length of the axis. |
| `compute_offset_reference`                    | `axis_x_start`, `axis_y_start`, `axis_x_end`, `axis_y_end`, `ref_line_x_start`, `ref_line_y_start`, `ref_line_x_end`, `ref_line_y_end`, `offset`                                      | Computes the projection of a point, `ref_point`, onto an axis. The axis is defined by the line segment from `(axis_x_start, axis_y_start)` to `(axis_x_end, axis_y_end)`. The point `ref_point` is at offset percentage distance along the reference line segment defined from `(ref_line_x_start, ref_line_y_start)` to `(ref_line_x_end, ref_line_y_end)`.                                                                                                                                                                                                                                                     |
| `render_landmark`                             | `label`, `x`, `y`                                                                                                                                                                     | Displays the landmark label and the `(x, y)` coordinate.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |

## Format Version

The format version applies to all JSON configuration files in AMPT. It may be incremented in the event of a backward-incompatible change. The `format_version` is used as a signal to AMPT about whether the configuration file needs to be updated before it can be parsed by the current release of the tool.

A higher numbered format version must always indicate a more recent version. If not explicitly set, the format version is implicitly zero.

