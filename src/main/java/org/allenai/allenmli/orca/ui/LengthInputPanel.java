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

import org.allenai.allenmli.orca.data.DataStore;
import org.allenai.allenmli.orca.data.Point;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.RoiListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.Vector;
import java.util.stream.Collectors;

/** Input panel that is used for managing length measurements. */
public class LengthInputPanel extends InputPanel implements ItemListener, RoiListener {

  // UI Elements
  private JCheckBox enableOverlays;
  private JComboBox<String> measurementSelector;
  private JTextField currentLength;
  private JTextField savedLength;
  private JTextField statusField;
  private JButton saveButton;
  private JButton revertButton;
  private JButton clearButton;
  private JButton approveButton;

  // State Elements
  private Point[] currentLine;
  private Double currentMagnitude;
  private Point[] savedLine;
  private Double savedMagnitude;
  private boolean reviewState;

  /**
   * Constructs a LengthInputPanel
   *
   * @param dataStore the DataStore that is used to hold the measurement data.
   * @param cueManager the CueManager that used to render measurement cues.
   */
  public LengthInputPanel(DataStore dataStore, CueManager cueManager) {
    super(dataStore, cueManager);
    Line.addRoiListener(this);
    reload_fields();
  }

  /**
   * Handle changes to the active measurement.
   *
   * @param e event that describing the change.
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    if (ItemEvent.SELECTED == e.getStateChange()) {
      cueManager.setActiveCue((String) measurementSelector.getSelectedItem());
      lastActiveImage.getMostRecentImageWindow().deleteRoi();
      reload_fields();
    }
  }

  /** Responsible for populating the user interface components. */
  protected void buildUI() {
    final Vector<String> measurements =
        dataStore.descriptors.values().stream() // Grab a stream of descriptors
            .filter(s -> "length".equals(s.measurement_type)) // Grab the length descriptors.
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort them by descriptor index
            .map(s -> s.name) // Extract the names
            .collect(Collectors.toCollection(Vector::new)); // Make a vector for JComboBox

    GridBagConstraints gbc;
    setLayout(new GridBagLayout());
    enableOverlays = new JCheckBox();
    enableOverlays.setModel(cueManager.cueToggle);
    enableOverlays.setText("Cues");
    controls.add(enableOverlays);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 8;
    add(enableOverlays, gbc);

    measurementSelector = new JComboBox<>(measurements);
    controls.add(measurementSelector);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 8;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(measurementSelector, gbc);

    final JLabel currentLabel = new JLabel();
    currentLabel.setText("Current");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    add(currentLabel, gbc);

    currentLength = new JTextField();
    currentLength.setColumns(8);
    currentLength.setText("");
    currentLength.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(currentLength, gbc);

    final JLabel savedLabel = new JLabel();
    savedLabel.setText("Saved");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    add(savedLabel, gbc);

    savedLength = new JTextField();
    savedLength.setColumns(8);
    savedLength.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(savedLength, gbc);

    saveButton = new JButton();
    saveButton.setText("Save");
    controls.add(saveButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(saveButton, gbc);

    revertButton = new JButton();
    revertButton.setText("Revert");
    controls.add(revertButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(revertButton, gbc);

    approveButton = new JButton();
    approveButton.setText("Approve");
    controls.add(approveButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(approveButton, gbc);

    clearButton = new JButton();
    clearButton.setText("Clear");
    controls.add(clearButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(clearButton, gbc);

    final JLabel lengthUnitLabel = new JLabel();
    lengthUnitLabel.setHorizontalAlignment(SwingConstants.CENTER);
    lengthUnitLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    lengthUnitLabel.setText("Length (px)");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 2;
    add(lengthUnitLabel, gbc);

    final JLabel statusLabel = new JLabel();
    statusLabel.setText("Status");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    add(statusLabel, gbc);

    statusField = new JTextField();
    statusField.setColumns(8);
    statusField.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(statusField, gbc);

    // Spacers
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    final JPanel spacer11 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 9;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(new JPanel(), gbc);
  }

  /** Responsible for configuring the event handling. */
  protected void wireUI() {
    saveButton.addActionListener(this::save);
    revertButton.addActionListener(this::revert);
    clearButton.addActionListener(this::clear);
    approveButton.addActionListener(this::approve);
    measurementSelector.addItemListener(this);
    enableOverlays.addActionListener(e -> updateInterface());
  }

  /**
   * Stash the current ROI and set the reviewed flag to false.
   *
   * @param e the event the triggers the save action
   */
  @Override
  protected void save(ActionEvent e) {
    final String reviewColumn = String.format("%s_reviewed", measurementSelector.getSelectedItem());
    if (null != currentLine) { // Save the selected line to the data store
      dataStore.set_endpoints(
          lastActiveImage.getMostRecentImageName(),
          (String) measurementSelector.getSelectedItem(),
          currentLine[0],
          currentLine[1]);
      savedLine = currentLine.clone();
    } else { // Clear the selected line in the datastore
      dataStore.set_endpoints(
          lastActiveImage.getMostRecentImageName(),
          (String) measurementSelector.getSelectedItem(),
          null,
          null);
      savedLine = null;
    }

    // Update the magnitude in the datastore
    dataStore.insert_value(
        lastActiveImage.getMostRecentImageName(),
        (String) measurementSelector.getSelectedItem(),
        currentMagnitude);
    dataStore.insert_value(lastActiveImage.getMostRecentImageName(), reviewColumn, false);

    // This may be unneeded, but it serves as a sanity check.
    savedMagnitude =
        dataStore.get_value(
            lastActiveImage.getMostRecentImageName(),
            (String) measurementSelector.getSelectedItem(),
            Double.class,
            null);
    reviewState =
        dataStore.get_value(
            lastActiveImage.getMostRecentImageName(), reviewColumn, Boolean.class, false);
    updateInterface();
  }

  /**
   * Revert the current length.
   *
   * @param e the event the triggers the revert action
   */
  @Override
  protected void revert(ActionEvent e) {
    lastActiveImage.getMostRecentImageWindow().deleteRoi();
    reload_fields();
  }

  /**
   * Clear the current length
   *
   * @param e the event the triggers the clear action
   */
  @Override
  protected void clear(ActionEvent e) {
    lastActiveImage.getMostRecentImageWindow().deleteRoi();
  }

  /**
   * Approve the saved length.
   *
   * @param e the event the triggers the approve action
   */
  @Override
  protected void approve(ActionEvent e) {
    final String reviewColumn = String.format("%s_reviewed", measurementSelector.getSelectedItem());
    dataStore.insert_value(lastActiveImage.getMostRecentImageName(), reviewColumn, true);
    reviewState =
        dataStore.get_value(
            lastActiveImage.getMostRecentImageName(), reviewColumn, Boolean.class, false);
    updateInterface();
  }

  /** Rerender the values in the UI. */
  @Override
  public void updateInterface() {
    if (!isVisible()) {
      return;
    }

    if (null == savedMagnitude) {
      savedLength.setText("");
    } else {
      savedLength.setText(String.format("%.3f", savedMagnitude));
    }

    if (null == currentMagnitude) {
      currentLength.setText("");
    } else {
      currentLength.setText(String.format("%.3f", currentMagnitude));
    }

    if (lastActiveImage.no_images()) {
      statusField.setText("");
    } else if (reviewState) {
      statusField.setText(DataStore.ACCEPTED);
    } else {
      statusField.setText(DataStore.UNREVIEWED);
    }

    final ImagePlus img = lastActiveImage.getMostRecentImageWindow();
    if (null == img) {
      return;
    }

    cueManager.draw();
    final Roi active_roi = img.getRoi();
    if (null == active_roi && null != currentLine) {
      img.setRoi(new Line(currentLine[0].x, currentLine[0].y, currentLine[1].x, currentLine[1].y));
    }
  }

  /** Load all of the state data. */
  @Override
  public void reload_fields() {
    if (lastActiveImage.no_images()) {
      savedMagnitude = null;
      currentMagnitude = null;
      savedLine = null;
      currentLine = null;
    } else {
      savedMagnitude =
          (Double)
              dataStore.get_value(
                  lastActiveImage.getMostRecentImageName(),
                  (String) measurementSelector.getSelectedItem());
      savedLine =
          dataStore.getEndpoints(
              lastActiveImage.getMostRecentImageName(),
              (String) measurementSelector.getSelectedItem());

      if (null != savedMagnitude && null != savedLine) {
        currentMagnitude = savedMagnitude;
        currentLine = savedLine.clone();
      } else {
        currentMagnitude = null;
        currentLine = null;
      }

      final String reviewColumn =
          String.format("%s_reviewed", measurementSelector.getSelectedItem());
      reviewState =
          dataStore.get_value(
              lastActiveImage.getMostRecentImageName(), reviewColumn, Boolean.class, false);
    }
    updateInterface();
  }

  /**
   * Makes the component visible or invisible. Sets the activeCue in CueManager to the selected
   * measurement.
   *
   * <p>Overrides <code>Component.setVisible</code>.
   *
   * @param aFlag true to make the component visible; false to make it invisible
   */
  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (aFlag) {
      cueManager.setActiveCue((String) measurementSelector.getSelectedItem());
    }
  }

  /**
   * Invoked when the region of interest is changed.
   *
   * @param imp the image whose ROI has changed.
   * @param id the action that happened.
   */
  @Override
  public void roiModified(ImagePlus imp, int id) {
    if (null == imp || !imp.getTitle().equals(lastActiveImage.getMostRecentImageName())) {
      return;
    }

    if (RoiListener.DELETED == id) {
      currentLine = null;
      currentMagnitude = null;
    } else {
      final Roi raw_roi = imp.getRoi();
      if (Roi.LINE != raw_roi.getType()) {
        return;
      }

      final Line lineRoi = (Line) raw_roi;
      if (null == currentLine) {
        currentLine =
            new Point[] {new Point(lineRoi.x1d, lineRoi.y1d), new Point(lineRoi.x2d, lineRoi.y2d)};
      } else {
        currentLine[0].setLocation(lineRoi.x1d, lineRoi.y1d);
        currentLine[1].setLocation(lineRoi.x2d, lineRoi.y2d);
      }
      currentMagnitude = lineRoi.getLength();
    }
    updateInterface();
  }
}
