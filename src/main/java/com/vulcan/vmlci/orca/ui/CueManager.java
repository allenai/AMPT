/*
 * Copyright Vulcan Inc. 2021
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

public class CueManager {
  private DataStore dataStore;
  private LastActiveImage lastActiveImage;
  private String config_file = "CueConfig.json";
  private HashMap<String, Vector<String>> cue_lookup;
  private ReferenceCalculator referenceCalculator;

  public CueManager(DataStore dataStore)
      throws FileNotFoundException, ConfigurationFileLoadException {
    this.dataStore = dataStore;
    this.lastActiveImage = LastActiveImage.getInstance();
    cue_lookup = new HashMap<>();

    referenceCalculator = new ReferenceCalculator(dataStore);
    load_configuration();
  }

  private void load_configuration() throws ConfigurationFileLoadException {
    HashMap<String, Object> cue_options = ConfigurationLoader.get_json_file(this.config_file);
    for (String cue_name : cue_options.keySet()) {
      for (Object cue_option : (Object[]) cue_options.get(cue_name)) {
        cue_lookup.putIfAbsent((String) cue_option, new Vector<>());
        cue_lookup.get((String) cue_option).add(cue_name);
      }
    }
  }

  /**
   * Draws cue lines on the image to aid measurement.
   *
   * @param measurement The current active measurement
   */
  public void draw_cue(String measurement) {
    if (lastActiveImage.getMostRecentImageName().equals(LastActiveImage.NO_OPEN_IMAGE)) {
      return;
    }
    String image_name = lastActiveImage.getMostRecentImageName();
    ImagePlus image = lastActiveImage.getMostRecentImageWindow();
    image.setOverlay(null);

    if (!cue_lookup.containsKey(measurement)) {
      return;
    }
    Overlay overlay = new Overlay();
    overlay.drawNames(true);
    overlay.drawLabels(true);
    overlay.setLabelFontSize(16, "");
    for (String cue : cue_lookup.get(measurement)) {
      HashMap<String, Point[]> guideline;
      guideline = (HashMap<String, Point[]>) referenceCalculator.do_measurement(cue, image_name);
      for (String label : guideline.keySet()) {
        Point[] endpoints = guideline.get(label);
        if (label.equals("axis")) {
          label = "";
        }
        switch (endpoints.length) {
          case 1:
            overlay.add(new PointRoi(endpoints[0].x, endpoints[0].y), label);
            break;
          case 2:
            overlay.add(
                new Line(endpoints[0].x, endpoints[0].y, endpoints[1].x, endpoints[1].y), label);
            break;
        }
      }
    }
    image.setOverlay(overlay);
  }
}
