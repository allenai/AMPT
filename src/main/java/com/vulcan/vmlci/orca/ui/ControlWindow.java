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

import com.vulcan.vmlci.orca.calculator.MeasurementManager;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.DataFileLoadException;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The <code>ControlWindow</code> class is the main UI for the Aquatic Mammal Photogrammetry Tool.
 */
public class ControlWindow implements ActiveImageListener, TableModelListener {
  private final JPanel metadata = null;
  private final JPanel input = null;
  private final JPanel length_measurements = null;
  private final JPanel body_profiles = null;
  private JFrame application_frame;
  private DataStore ds;
  private CueManager cueManager;
  private MetadataControl metadataControl;
  private InputControls inputControls;
  private String active_image;

  public ControlWindow() {
    final Logger logger = new StderrLogService();
    try {
      ds = DataStore.createDataStore();
    } catch (final ConfigurationFileLoadException | DataFileLoadException e) {
      logger.error(e);
    }
    try {
      cueManager = new CueManager(ds);
    } catch (final ConfigurationFileLoadException e) {
      logger.error(e);
    }

    SwingUtilities.invokeLater(this::build_ui);
    ds.addTableModelListener(this);
    try {
      final MeasurementManager measurementManager = new MeasurementManager(ds);
    } catch (final ConfigurationFileLoadException e) {
      logger.error(e);
    }
  }

  /** Layout the user interface */
  private void build_ui() {
    final LastActiveImage lastActiveImage = LastActiveImage.getInstance();
    metadataControl = new MetadataControl(ds, cueManager);
    inputControls = new InputControls(ds, cueManager);
    final GridBagConstraints c = new GridBagConstraints();
    final JPanel toplevel = new JPanel();
    toplevel.setLayout(new GridBagLayout());

    GridBagConstraints gbc;

    // Constraints for branding
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Branding
    final JPanel branding = new Branding();
    toplevel.add(branding, gbc);

    // Center Space
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    toplevel.add(build_accordion(), gbc);

    // Data Controls
    final JPanel csv_controls = new DataControls(ds);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    toplevel.add(csv_controls, gbc);

    application_frame = new JFrame();
    application_frame.setTitle("Test Window");
    application_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    application_frame.add(toplevel);
    application_frame.pack();
    application_frame.setVisible(true);

    active_image = lastActiveImage.getMostRecentImageName();
    lastActiveImage.addActiveImageListener(this);
    setTitle();
  }

  private JComponent build_accordion() {

    final JPanel frame = new JPanel();
    final JScrollPane scrollPane = new JScrollPane(frame);
    GridBagConstraints gbc = new GridBagConstraints();
    frame.setLayout(new GridBagLayout());

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 0;
    final AccordionPanel metadata = new AccordionPanel("Metadata", true);
    frame.add(metadata, gbc);

    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = 0;
    metadata.setContent_panel(metadataControl);
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 1;
    final AccordionPanel demo2 = new AccordionPanel("Input", true);
    demo2.setContent_panel(inputControls.inputPanel);
    frame.add(demo2, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 2;
    final AccordionPanel measurements = new AccordionPanel("Length Measurements", true);
    final JPanel lengthMeasurements =
        new LengthDisplay(
            ds, s -> s.measurement_type.contains("length") && !s.name.contains("%"), cueManager);
    measurements.setContent_panel(lengthMeasurements);
    frame.add(measurements, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 3;
    final AccordionPanel bodyProfiles = new AccordionPanel("Body Profiles", true);
    final JPanel profileMeasurements =
        new LengthDisplay(
            ds, s -> s.measurement_type.contains("length") && s.name.contains("%"), cueManager);
    bodyProfiles.setContent_panel(profileMeasurements);
    frame.add(bodyProfiles, gbc);

    final JPanel spacer = new JPanel();
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.VERTICAL;
    frame.add(spacer, gbc);
    return scrollPane;
  }

  /**
   * Generate and set the title for the window. Title is based on the currently open file and the
   * dirty state of the dataStore.
   */
  private void setTitle() {
    final String saved_state;
    if (ds.dirty()) {
      saved_state = "CSV Unsaved";
    } else {
      saved_state = "CSV Saved";
    }
    application_frame.setTitle(String.format("Measuring: %s  - %s", active_image, saved_state));
  }

  /** @param evt the ActiveImageChangeEvent used for constructing the image title. */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    active_image = evt.getNewImage();
    setTitle();
  }

  /**
   * The event handler supports updating the title to reflect the dirty status of the dataStore.
   *
   * @param e Event containing the details of the table change.
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    setTitle();
  }
}
