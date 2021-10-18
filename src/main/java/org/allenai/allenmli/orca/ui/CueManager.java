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

package org.allenai.allenmli.orca.ui;

import com.cedarsoftware.util.io.JsonObject;
import org.allenai.allenmli.orca.calculator.ReferenceCalculator;
import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.Point;
import org.allenai.allenmli.orca.helpers.ConfigurationFile;
import org.allenai.allenmli.orca.helpers.ConfigurationFileLoadException;
import org.allenai.allenmli.orca.helpers.ConfigurationLoader;
import org.allenai.allenmli.orca.helpers.LastActiveImage;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;

import javax.swing.JToggleButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CueManager {
  public final JToggleButton.ToggleButtonModel cueToggle;
  public final JToggleButton.ToggleButtonModel overlayToggle;
  private final DataStore dataStore;
  private final LastActiveImage lastActiveImage;
  private final HashMap<String, ArrayList<String>> cue_lookup;
  private final ReferenceCalculator referenceCalculator;
  private final HashSet<String> conditionLines = new HashSet<>();
  /** The measurement cue selected for rendering */
  private String activeCue;

  public CueManager(DataStore dataStore) throws ConfigurationFileLoadException {
    this.dataStore = dataStore;
    referenceCalculator = new ReferenceCalculator(dataStore);
    lastActiveImage = LastActiveImage.getInstance();
    cue_lookup = new HashMap<>();

    cueToggle = new JToggleButton.ToggleButtonModel();
    overlayToggle = new JToggleButton.ToggleButtonModel();

    load_configuration();

    // Configure toggles (essentially a radio group at allows de-selection).
    cueToggle.addActionListener(
        e -> {
          if (cueToggle.isSelected()) {
            overlayToggle.setSelected(false);
          }
        });
    overlayToggle.addActionListener(
        e -> {
          if (overlayToggle.isSelected()) {
            cueToggle.setSelected(false);
          }
        });
  }

  private void load_configuration() throws ConfigurationFileLoadException {
    final HashMap<String, Object> cue_config =
        ConfigurationLoader.getJsonFileAsMap(ConfigurationFile.CUE_CONFIG.getFilename());
    final String configuration_name = "configuration";
    if (!cue_config.containsKey(configuration_name)) {
      throw new ConfigurationFileLoadException(
          String.format(
              "%s is missing required field: \"%s\"",
              ConfigurationFile.CUE_CONFIG.getFilename(), configuration_name));
    }
    final Object[] loaded_items = (Object[]) cue_config.get(configuration_name);
    for (final Object raw_item : loaded_items) {
      final JsonObject<String, Object> json_item = (JsonObject<String, Object>) raw_item;
      final String cue = (String) json_item.get("cue");
      final Object[] measurements = (Object[]) json_item.get("measurements");
      for (Object raw_measurement : measurements) {
        final String measurement = (String) raw_measurement;
        cue_lookup.putIfAbsent(measurement, new ArrayList<>());
        cue_lookup.get(measurement).add(cue);
      }
    }
  }

  /**
   * Returns the cue that cue manager will draw.
   *
   * @return the currently active cue
   */
  public String getActiveCue() {
    return activeCue;
  }

  /**
   * Select the next visible cue.
   *
   * @param activeCue the cue to draw when appropriate.
   */
  public void setActiveCue(String activeCue) {
    this.activeCue = activeCue;
  }

  public boolean getConditionLine(String condition_line) {
    return conditionLines.contains(condition_line);
  }

  public void setConditionLine(String conditionLine, boolean draw) {
    if (draw) {
      conditionLines.add(conditionLine);
    } else {
      conditionLines.remove(conditionLine);
    }
  }

  /** Update the cue/condition line overaly. */
  public void draw() {
    if (lastActiveImage.no_images()) {
      return;
    }
    final String image_name = lastActiveImage.getMostRecentImageName();
    final ImagePlus img = lastActiveImage.getMostRecentImageWindow();

    if (cueToggle.isSelected()) {
      img.setOverlay(draw_cue(image_name));
    } else if (overlayToggle.isSelected()) {
      img.setOverlay(drawOverlays(image_name));
    } else {
      img.setOverlay(null);
    }
  }

  /**
   * Draws cue lines on the image to aid measurement.
   *
   * @param image_name The image being drawn on.
   */
  private Overlay draw_cue(String image_name) {
    if (!cue_lookup.containsKey(activeCue)) {
      return null;
    }
    final Overlay overlay = new Overlay();
    overlay.drawNames(true);
    overlay.drawLabels(true);
    overlay.setLabelFontSize(16, "");
    for (final String cue : cue_lookup.get(activeCue)) {
      final HashMap<String, Point[]> guideline;
      guideline = (HashMap<String, Point[]>) referenceCalculator.do_measurement(cue, image_name);
      for (String label : guideline.keySet()) {
        final Point[] endpoints = guideline.get(label);
        if ("axis".equals(label)) {
          //noinspection AssignmentToForLoopParameter
          label = "";
        }
        Roi marker = null;
        switch (endpoints.length) {
          case 1:
            marker = new PointRoi(endpoints[0].x, endpoints[0].y);
            break;
          case 2:
            marker = new Line(endpoints[0].x, endpoints[0].y, endpoints[1].x, endpoints[1].y);
            break;
        }
        if (null != marker) {
          marker.setName(label);
          overlay.add(marker, label);
        }
      }
    }
    return overlay;
  }

  /** Draws the condition lines specified via addConditionLine amd removeConditionLine */
  public Overlay drawOverlays(String image_name) {
    if (conditionLines.isEmpty()) {
      return null;
    }
    final Overlay overlay = new Overlay();
    for (final String conditionLine : conditionLines) {
      HashMap<String, Point[]> guideline;
      final Point[] endpoints = dataStore.getEndpoints(image_name, conditionLine);
      if (null != endpoints) {
        overlay.add(new Line(endpoints[0].x, endpoints[0].y, endpoints[1].x, endpoints[1].y));
      }
    }
    return overlay;
  }
}
