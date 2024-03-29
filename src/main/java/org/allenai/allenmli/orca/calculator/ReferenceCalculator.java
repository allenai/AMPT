/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
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

package org.allenai.allenmli.orca.calculator;

import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.Point;
import org.allenai.allenmli.orca.helpers.ConfigurationFile;
import org.allenai.allenmli.orca.helpers.ConfigurationFileLoadException;

import java.util.HashMap;

import static java.lang.Math.sqrt;

public class ReferenceCalculator extends BaseCalculator {
  /**
   * Construct a
   *
   * @param ds the <code>DataStore</code> that the Calculator will operate on.
   * @throws ConfigurationFileLoadException when the configuration file is not present.
   */
  public ReferenceCalculator(DataStore ds) throws ConfigurationFileLoadException {
    super(ds);
  }

  /**
   * Construct and return a hashmap that contains reference markers.
   *
   * @param axis_x_start the x coordinate of the start of the axis
   * @param axis_y_start the y coordinate of the start of the axis
   * @param axis_x_end the x coordinate of the end of the axis
   * @param axis_y_end the y coordinate of the end of the axis
   * @param start_percentage the percentage as an integer of the length of the length basis for the
   *     first marker
   * @param end_percentage the percentage as an integer of the length of the length basis for the
   *     maximum marker
   * @param step_size The percentage as an integer step. This means the finest resolution is 1%
   * @return a <code>HashMap&lt;String, Point2D[]&gt;</code> containing the markers and the
   *     reference axis.
   */
  public static HashMap<String, Point[]> interval_reference_markers(
      Double axis_x_start,
      Double axis_y_start,
      Double axis_x_end,
      Double axis_y_end,
      Long start_percentage,
      Long end_percentage,
      Long step_size) {
    return interval_reference_markers_with_base_length(
        axis_x_start,
        axis_y_start,
        axis_x_end,
        axis_y_end,
        axis_x_start,
        axis_y_start,
        axis_x_end,
        axis_y_end,
        start_percentage,
        end_percentage,
        step_size,
        0L);
  }

  /**
   * Construct and return a dictionary that contains reference markers.
   *
   * <p>This is for the case where a reference line is drawn with percentage markers along the line
   * where the marker spacing may be derived from the length of a different line.
   *
   * @param axis_x_start s coordinate of starting point of line being draw along.
   * @param axis_y_start y coordinate of starting point of line being draw along.
   * @param axis_x_end x coordinate of ending point of line begin drawn along
   * @param axis_y_end y coordinate of ending point of line begin drawn along
   * @param ref_x_start x coordinate of starting point of line used for length reference.
   * @param ref_y_start y coordinate of starting point of line used for length reference.
   * @param ref_x_end x coordinate of ending point of line used for length reference.
   * @param ref_y_end y coordinate of ending point of line used for length reference.
   * @param start_percentage the percentage as an integer of the length of the length basis for the
   *     first marker
   * @param end_percentage The percentage as an integer of the length of the length basis for the
   *     maximum marker
   * @param step_size The percentage as an integer step. This means the finest resolution is 1%
   * @param label_offset Offset value used to generate labels
   * @return a <code>HashMap&lt;String, Point2D[]&gt;</code> containing the markers and the
   *     reference axis
   */
  public static HashMap<String, Point[]> interval_reference_markers_with_base_length(
      Double axis_x_start,
      Double axis_y_start,
      Double axis_x_end,
      Double axis_y_end,
      Double ref_x_start,
      Double ref_y_start,
      Double ref_x_end,
      Double ref_y_end,
      Long start_percentage,
      Long end_percentage,
      Long step_size,
      Long label_offset) {

    // Compute the deltas for the line being drawn.
    double axis_x_delta = axis_x_end - axis_x_start;
    double axis_y_delta = axis_y_end - axis_y_start;

    // Compute the marker offsets.
    final double marker_x_offset = -axis_y_delta * 0.025;
    final double marker_y_offset = axis_x_delta * 0.025;

    // Unitize the deltas
    final double axis_length = sqrt(axis_x_delta * axis_x_delta + axis_y_delta * axis_y_delta);
    axis_x_delta /= axis_length;
    axis_y_delta /= axis_length;

    // Compute the reference length
    final double reference_length =
        sqrt(
            (ref_x_end - ref_x_start) * (ref_x_end - ref_x_start)
                + (ref_y_end - ref_y_start) * (ref_y_end - ref_y_start));

    final HashMap<String, Point[]> result = new HashMap<>();

    for (double percentage = start_percentage;
        percentage <= end_percentage;
        percentage += step_size) {
      if ((percentage * reference_length) / 100. <= axis_length) {
        final double offset = percentage * reference_length / 100.;
        final double marker_x = axis_x_start + axis_x_delta * offset;
        final double marker_y = axis_y_start + axis_y_delta * offset;
        final String label = String.format("%d%%", (int) (percentage + label_offset));
        result.put(
            label,
            new Point[] {
              new Point(marker_x + marker_x_offset, marker_y + marker_y_offset),
              new Point(marker_x - marker_x_offset, marker_y - marker_y_offset)
            });
      }
    }

    result.put(
        "axis",
        new Point[] {new Point(axis_x_start, axis_y_start), new Point(axis_x_end, axis_y_end)});
    return result;
  }

  /**
   * Draws a line from (ref_line_x_start, ref_line_y_start) to (ref_line_x_end, ref_line_y_end) and
   * a marker at offset% along the line. The marker gets a label of the form "%d%%".format(offset)
   *
   * @param ref_line_x_start the x coordinate of the start of the line
   * @param ref_line_y_start the y coordinate of the start of the line
   * @param ref_line_x_end the x coordinate of the end of the line
   * @param ref_line_y_end the y coordinate of the end of the line
   * @param offset The percentage offset from start along the line. Note the 1% resolution.
   * @return a reference line with a point at offset% along the line.
   */
  public static HashMap<String, Point[]> draw_ref_along_line(
      Double ref_line_x_start,
      Double ref_line_y_start,
      Double ref_line_x_end,
      Double ref_line_y_end,
      Long offset) {
    // Compute reference point along ref line
    final double ref_point_x =
        ref_line_x_start + (offset / 100.) * (ref_line_x_end - ref_line_x_start);
    final double ref_point_y =
        ref_line_y_start + (offset / 100.) * (ref_line_y_end - ref_line_y_start);

    final HashMap<String, Point[]> result = new HashMap<>();
    result.put(
        "",
        new Point[] {
          new Point(ref_line_x_start, ref_line_y_start), new Point(ref_line_x_end, ref_line_y_end)
        });
    result.put(String.format("%d%%", offset), new Point[] {new Point(ref_point_x, ref_point_y)});
    return result;
  }

  /**
   * Computes a set of offset reference markers along an axis. The markers are returned in a HashMap
   * The axis, defined by the line segment from (axis_x_start, axis_y_start) to (axis_x_end,
   * axis_y_end) is keyed by 'axis'. Two line segments orthogonal to the 'axis' line segment, 'ref
   * top' and 'ref bottom', would intersect the points (ref_line_x_start, ref_line_y_start) and
   * (ref_line_x_end, ref_line_y_end) respectively. A third line segment orthogonal to 'axis', keyed
   * by '&lt;offest&gt;% measurement', would intersect the point that is offset% along the line
   * segment defined from (ref_line_x_start, ref_line_y_start) to (ref_line_x_end, ref_line_y_end).
   * The lengths of the reference lines are 5% of the length of axis.
   *
   * @param axis_x_start the x coordinate of the start of the line being projected onto
   * @param axis_y_start the y coordinate of the start of the line being projected onto
   * @param axis_x_end the x coordinate of the end of the line being projected onto
   * @param axis_y_end the y coordinate of the end of the line being projected onto
   * @param ref_line_x_start the x coordinate of the start of the line being projected from
   * @param ref_line_y_start the y coordinate of the start of the line being projected from
   * @param ref_line_x_end the x coordinate of the end of the line being projected from
   * @param ref_line_y_end the y coordinate of the end of the line being projected from
   * @param offset The percentage offset from start along the line being projected from. Note the 1%
   *     resolution.
   * @return return a reference line with offset markers along the long.
   */
  public static HashMap<String, Point[]> compute_offset_reference_markers(
      Double axis_x_start,
      Double axis_y_start,
      Double axis_x_end,
      Double axis_y_end,
      Double ref_line_x_start,
      Double ref_line_y_start,
      Double ref_line_x_end,
      Double ref_line_y_end,
      Long offset) {
    final Point top =
        compute_offset_reference(
            axis_x_start,
            axis_y_start,
            axis_x_end,
            axis_y_end,
            ref_line_x_start,
            ref_line_y_start,
            ref_line_x_end,
            ref_line_y_end,
            0L);

    final Point bot =
        compute_offset_reference(
            axis_x_start,
            axis_y_start,
            axis_x_end,
            axis_y_end,
            ref_line_x_start,
            ref_line_y_start,
            ref_line_x_end,
            ref_line_y_end,
            100L);

    final Point ref =
        compute_offset_reference(
            axis_x_start,
            axis_y_start,
            axis_x_end,
            axis_y_end,
            ref_line_x_start,
            ref_line_y_start,
            ref_line_x_end,
            ref_line_y_end,
            offset);
    // generated deltas for reference line. Rotate axis by 90 degrees
    final double d_x = (axis_y_start - axis_y_end) * 0.025;
    final double d_y = (axis_x_end - axis_x_start) * 0.025;
    final HashMap<String, Point[]> result = new HashMap<>();
    result.put(
        "axis",
        new Point[] {new Point(axis_x_start, axis_y_start), new Point(axis_x_end, axis_y_end)});
    result.put(
        "ref top",
        new Point[] {new Point(top.x - d_x, top.y - d_y), new Point(top.x + d_x, top.y + d_y)});
    result.put(
        String.format("%d%% measurement", offset),
        new Point[] {new Point(ref.x - d_x, ref.y - d_y), new Point(ref.x + d_x, ref.y + d_y)});
    result.put(
        "ref bottom",
        new Point[] {new Point(bot.x - d_x, bot.y - d_y), new Point(bot.x + d_x, bot.y + d_y)});
    return result;
  }

  /**
   * Computes the projection proj of ref_point onto axis where ref_points is some distance along a
   * reference line.
   *
   * <pre>
   *                  | (axis_x_start, axis_y_start)
   *                  |
   *                  |
   *                  |    \ (ref_line_x_start, ref_line_y_start)
   *                  |     \
   *                  |      \
   * (proj_x, proj_y) +       + (ref_point_x, ref_point_y)
   *                  |        \ (ref_line_x_ebd, ref_line_y_end)
   *                  |
   *                  | (axis_x_end, axis_y_end)
   *
   * </pre>
   *
   * @param axis_x_start the x coordinate for the starting point of the axis
   * @param axis_y_start the y coordinate for the starting point of the axis
   * @param axis_x_end the x coordinate for the ending point of the axis
   * @param axis_y_end the y coordinate for the ending point of the axis
   * @param ref_line_x_start the x coordinate for the starting point of ref_line
   * @param ref_line_y_start the y coordinate for the starting point of ref_line
   * @param ref_line_x_end the x coordinate for the ending point of ref_line
   * @param ref_line_y_end the x coordinate for the ending point of ref_line
   * @param offset percentage along ref_line for ref_point.
   * @return the coordinates for proj.
   */
  public static Point compute_offset_reference(
      Double axis_x_start,
      Double axis_y_start,
      Double axis_x_end,
      Double axis_y_end,
      Double ref_line_x_start,
      Double ref_line_y_start,
      Double ref_line_x_end,
      Double ref_line_y_end,
      Long offset) {

    // Compute direction vector for main axis
    double axis_direction_x = axis_x_end - axis_x_start;
    double axis_direction_y = axis_y_end - axis_y_start;
    final double length =
        sqrt(axis_direction_x * axis_direction_x + axis_direction_y * axis_direction_y);
    axis_direction_x /= length;
    axis_direction_y /= length;

    // Compute reference point along ref line
    final double ref_point_x =
        ref_line_x_start + (offset / 100.) * (ref_line_x_end - ref_line_x_start);
    final double ref_point_y =
        ref_line_y_start + (offset / 100.) * (ref_line_y_end - ref_line_y_start);

    // Compute vector from axis start to reference point
    final double c_x = ref_point_x - axis_x_start;
    final double c_y = ref_point_y - axis_y_start;

    // Compute projection of vector (c_x, c_y) onto axis.
    final double distance_along_axis = (c_x * axis_direction_x + c_y * axis_direction_y);

    // Compute the location of the projection of the ref_point onto the axis.
    return new Point(
        axis_x_start + distance_along_axis * axis_direction_x,
        axis_y_start + distance_along_axis * axis_direction_y);
  }

  /**
   * @param label The label to be used for drawing the landmark
   * @param x The x-coordinate of the landmark
   * @param y The y-coordinate of the landmark
   * @return A HashMap containing the landmark rendering instructions.
   */
  public static HashMap<String, Point[]> render_landmark(String label, Double x, Double y) {
    final HashMap<String, Point[]> result = new HashMap<>();
    result.put(label, new Point[] {new Point(x, y)});
    return result;
  }

  /** @return The name of the configuration file required for this calculator. */
  @Override
  protected String getConfigurationFile() {
    return ConfigurationFile.REFERENCE_CONFIG.getFilename();
  }

  /**
   * Perform measurement <code>measure</code> on <code>title</code>.
   *
   * <p>Before performing the calculation, the parameters are checked to ensure that they're
   * non-null.
   *
   * @param measure the name of the measurement.
   * @param title the image to measure.
   * @return the measurement. May be null if all parameters are not present.
   * @throws IllegalArgumentException if a measure or measurement function is not available.
   */
  @Override
  public Object do_measurement(String measure, String title) {
    if (preflight_measurement(measure, title)) {
      return super.do_measurement(measure, title);
    }
    return new HashMap<String, Point[]>();
  }
}
