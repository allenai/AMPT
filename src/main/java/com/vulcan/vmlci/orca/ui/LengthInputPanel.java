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

import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;
import com.vulcan.vmlci.orca.data.Point;
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
import javax.swing.event.TableModelEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Vector;
import java.util.stream.Collectors;

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
  private Point[] currentLine = null;
  private Double currentMagnitude = null;
  private Point[] savedLine = null;
  private Double savedMagnitude = null;
  private boolean reviewState = false;

  public LengthInputPanel(DataStore dataStore, CueManager cueManager) {
    super(dataStore, cueManager);
    Line.addRoiListener(this);
  }

  /**
   * Invoked when an item has been selected or deselected by the user. The code written for this
   * method performs the operations that need to occur when an item is selected (or deselected).
   *
   * @param e event that describing the change.
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      cueManager.setActiveCue((String) measurementSelector.getSelectedItem());
      lastActiveImage.getMostRecentImageWindow().deleteRoi();
      reload_fields();
    }
  }

  /**
   * Makes the component visible or invisible.
   * Sets the activeCue in CueManager to the selected measurement.
   * <p>
   * Overrides <code>Component.setVisible</code>.
   *
   * @param aFlag true to make the component visible; false to make it invisible
   * @beaninfo attribute: visualUpdate true
   */
  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if(aFlag){
      cueManager.setActiveCue((String) measurementSelector.getSelectedItem());
    }
  }

  @Override
  public void reload_fields() {
    savedMagnitude =
        (Double)
            dataStore.get_value(
                lastActiveImage.getMostRecentImageName(),
                (String) measurementSelector.getSelectedItem());
    savedLine =
        dataStore.getEndpoints(
            lastActiveImage.getMostRecentImageName(),
            (String) measurementSelector.getSelectedItem());

    if (savedMagnitude != null) {
      currentMagnitude = savedMagnitude;
      if (savedLine != null) {
        currentLine = savedLine.clone();
      } else {
        currentLine = null;
      }
    } else {
      currentMagnitude = null;
      currentLine = null;
    }

    String reviewColumn = String.format("%s_reviewed", measurementSelector.getSelectedItem());
    reviewState =
        dataStore.get_value(
            lastActiveImage.getMostRecentImageName(), reviewColumn, Boolean.class, false);
    updateInterface();
  }

  protected void buildUI() {
    Vector<String> measurements =
        dataStore.descriptors.values().stream() // Grab a stream of descriptors
            .filter(s -> s.measurement_type.equals("length")) // Grab the length descriptors.
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort them by descriptor index
            .map(s -> s.name) // Extract the names
            .collect(Collectors.toCollection(Vector::new)); // Make a vector for JComboBox

    GridBagConstraints gbc;
    this.setLayout(new GridBagLayout());
    enableOverlays = new JCheckBox();
    enableOverlays.setModel(cueManager.cueToggle);
    enableOverlays.setText("Cues");
    controls.add(enableOverlays);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 8;
    this.add(enableOverlays, gbc);

    measurementSelector = new JComboBox<>(measurements);
    controls.add(measurementSelector);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 8;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(measurementSelector, gbc);

    final JLabel currentLabel = new JLabel();
    currentLabel.setText("Current");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(currentLabel, gbc);

    currentLength = new JTextField();
    currentLength.setColumns(8);
    currentLength.setText("");
    currentLength.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(currentLength, gbc);

    final JLabel savedLabel = new JLabel();
    savedLabel.setText("Saved");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(savedLabel, gbc);

    savedLength = new JTextField();
    savedLength.setColumns(8);
    savedLength.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(savedLength, gbc);

    saveButton = new JButton();
    saveButton.setText("Save");
    controls.add(saveButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(saveButton, gbc);

    revertButton = new JButton();
    revertButton.setText("Revert");
    controls.add(revertButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(revertButton, gbc);

    approveButton = new JButton();
    approveButton.setText("Approve");
    controls.add(approveButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(approveButton, gbc);

    clearButton = new JButton();
    clearButton.setText("Clear");
    controls.add(clearButton);
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(clearButton, gbc);

    final JLabel lengthUnitLabel = new JLabel();
    lengthUnitLabel.setHorizontalAlignment(SwingConstants.CENTER);
    lengthUnitLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    lengthUnitLabel.setText("Length (px)");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 2;
    this.add(lengthUnitLabel, gbc);

    final JLabel statusLabel = new JLabel();
    statusLabel.setText("Status");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(statusLabel, gbc);

    statusField = new JTextField();
    statusField.setColumns(8);
    statusField.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(statusField, gbc);

    // Spacers
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);

    final JPanel spacer11 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 9;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(new JPanel(), gbc);
  }

  protected void wireUI() {
    saveButton.addActionListener(this::save);
    revertButton.addActionListener(this::revert);
    clearButton.addActionListener(this::clear);
    approveButton.addActionListener(this::approve);
    measurementSelector.addItemListener(this);
    enableOverlays.addActionListener(e -> updateInterface());
  }

  @Override
  protected void save(ActionEvent e) {
    String reviewColumn = String.format("%s_reviewed", measurementSelector.getSelectedItem());
    if (currentLine != null) { // Save the selected line to the data store
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

  @Override
  protected void revert(ActionEvent e) {
    if (savedMagnitude != null) {
      lastActiveImage.getMostRecentImageWindow().deleteRoi();
      currentLine = savedLine.clone();
      currentMagnitude = savedMagnitude;
    } else {
      currentMagnitude = null;
      currentLine = null;
    }
    updateInterface();
  }

  @Override
  protected void clear(ActionEvent e) {
    lastActiveImage.getMostRecentImageWindow().deleteRoi();
  }

  @Override
  protected void approve(ActionEvent e) {
    String reviewColumn = String.format("%s_reviewed", measurementSelector.getSelectedItem());
    dataStore.insert_value(lastActiveImage.getMostRecentImageName(), reviewColumn, true);
    reviewState =
        dataStore.get_value(
            lastActiveImage.getMostRecentImageName(), reviewColumn, Boolean.class, false);
    updateInterface();
  }

  @Override
  public void updateInterface() {
    if(!this.isVisible()){
      return;
    }
    Roi roi;
    if (savedMagnitude != null) {
      savedLength.setText(String.format("%.3f", savedMagnitude));
    } else {
      savedLength.setText("");
    }

    String currMag = "";
    if (currentMagnitude != null) {
      currMag = String.format("%.3f", currentMagnitude);
    }
    currentLength.setText(currMag);

    if (lastActiveImage.no_images()) {
      statusField.setText("");
    } else if (reviewState) {
      statusField.setText(DataStore.ACCEPTED);
    } else {
      statusField.setText(DataStore.UNREVIEWED);
    }

    ImagePlus img = lastActiveImage.getMostRecentImageWindow();
    if (img == null) {
      return;
    }

    cueManager.draw();
    Roi active_roi = img.getRoi();
    if (active_roi == null && currentLine != null) {
      img.setRoi(new Line(currentLine[0].x, currentLine[0].y, currentLine[1].x, currentLine[1].y));
    }
  }

  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    super.activeImageChanged(evt);
    boolean measuring = !evt.getNewImage().equals(LastActiveImage.NO_OPEN_IMAGE);
    if (measuring) {
      String currentMeasurement = (String) measurementSelector.getSelectedItem();
      savedLine =
          dataStore.getEndpoints(lastActiveImage.getMostRecentImageName(), currentMeasurement);
      savedMagnitude =
          (Double)
              dataStore.get_value(lastActiveImage.getMostRecentImageName(), currentMeasurement);
    }
    updateInterface();
  }

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e event describing the change
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);
  }

  @Override
  public void roiModified(ImagePlus imp, int id) {
    if (imp == null || !imp.getTitle().equals(lastActiveImage.getMostRecentImageName())) {
      return;
    }

    if (id == RoiListener.DELETED) {
      currentLine = null;
      currentMagnitude = null;
    } else {
      Roi raw_roi = imp.getRoi();
      if (!(raw_roi instanceof Line)) {
        return;
      }
      Line roi = (Line) raw_roi;

      Rectangle2D.Double bounds = roi.getFloatBounds();
      if (currentLine == null) {
        currentLine = new Point[2];
        currentLine[0] = new Point();
        currentLine[1] = new Point();
      }
      currentLine[0].setLocation(roi.x1d, roi.y1d);
      currentLine[1].setLocation(roi.x2d, roi.y2d);
      currentMagnitude = roi.getLength();
    }
    updateInterface();
  }
}
