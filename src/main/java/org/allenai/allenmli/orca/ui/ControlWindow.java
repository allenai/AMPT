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

import org.allenai.allenmli.orca.calculator.MeasurementManager;
import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.event.ActiveImageChangeEvent;
import org.allenai.allenmli.orca.event.ActiveImageListener;
import org.allenai.allenmli.orca.helpers.ConfigurationFileLoadException;
import org.allenai.allenmli.orca.helpers.DataFileLoadException;
import org.allenai.allenmli.orca.helpers.LastActiveImage;
import ij.Executer;
import org.scijava.Context;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * The <code>ControlWindow</code> class is the main UI for the Aquatic Mammal Photogrammetry Tool.
 */
public class ControlWindow extends JFrame implements ActiveImageListener, TableModelListener {
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

  public ControlWindow(Context ctx) {
    final Logger logger = new StderrLogService();
    ctx.inject(this);
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
    final Insets insets = new Insets(0, 10, 0, 10); // Add left and right spacing around containers.
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
    gbc.insets = insets;

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
    gbc.insets = insets;
    toplevel.add(build_accordion(), gbc);

    // Data Controls
    final DataControls csv_controls = new DataControls(ds);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = insets;
    toplevel.add(csv_controls, gbc);

    application_frame = this;
    application_frame.setTitle("Test Window");
    application_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    application_frame.add(toplevel);
    application_frame.pack();
    application_frame.setVisible(true);

    active_image = lastActiveImage.getMostRecentImageName();
    lastActiveImage.addActiveImageListener(this);
    setTitle();
    this.addWindowFocusListener(
        new WindowFocusListener() {
          @Override
          public void windowGainedFocus(WindowEvent e) {
            ((JFrame) e.getSource()).setMenuBar(ij.Menus.getMenuBar());
          }

          @Override
          public void windowLostFocus(WindowEvent e) {}
        });
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            // Present a save option to the user if the current state is dirty.
            if (csv_controls.saveWithDiscardOption(false, false)) {
              dispose();
            }
          }
        });
    Executer.addCommandListener(
        command -> {
          if ("Quit".equals(command)) {
            try {
              csv_controls.save(false, false);
            } catch (DataSaveException e) {
              // ImageJ is shutting down at this point.
            }
          }
          return command;
        });
  }

  private JComponent build_accordion() {

    final JPanel frame = new JPanel();
    final JScrollPane scrollPane = new JScrollPane(frame);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
