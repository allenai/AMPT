/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.ui;

import com.vulcan.vmlci.orca.calculator.ReferenceCalculator;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.data.Point;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;
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
    final String CONFIG_FILE = "CueConfig.json";
    final HashMap<String, Object> cue_options = ConfigurationLoader.get_json_file(CONFIG_FILE);
    for (final String cue_name : cue_options.keySet()) {
      for (final Object cue_option : (Object[]) cue_options.get(cue_name)) {
        cue_lookup.putIfAbsent((String) cue_option, new ArrayList<>());
        cue_lookup.get(cue_option).add(cue_name);
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
