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
import com.vulcan.vmlci.orca.helpers.LastActiveImage;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.Vector;
import java.util.stream.Collectors;

public class LengthInputPanel extends InputPanel implements ItemListener, RoiListener {

  // UI Elements
  private JCheckBox enableOverlays;
  private JComboBox measurementSelector;
  private JTextField currentLength;
  private JTextField savedLength;
  private JButton save;
  private JButton revert;
  private JButton clear;

  // State Elements
  private Point2D.Double[] currentLine = null;
  private Double currentMagnitude = null;
  private Point2D.Double[] savedLine = null;
  private Double savedMagnitude = null;

  public LengthInputPanel(DataStore dataStore) {
    super(dataStore);
    Line.addRoiListener(this);
  }

  /**
   * Invoked when an item has been selected or deselected by the user. The code written for this
   * method performs the operations that need to occur when an item is selected (or deselected).
   *
   * @param e
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      lastActiveImage.getMostRecentImageWindow().deleteRoi();
      reload_fields();
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
      currentLine = savedLine.clone();
    } else {
      currentMagnitude = null;
      currentLine = null;
    }
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
    enableOverlays.setText("Overlays");
    controls.add(enableOverlays);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 7;
    this.add(enableOverlays, gbc);
    measurementSelector = new JComboBox<>(measurements);
    controls.add(measurementSelector);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 7;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(measurementSelector, gbc);
    final JLabel label5 = new JLabel();
    label5.setText("Current");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(label5, gbc);
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
    final JLabel label6 = new JLabel();
    label6.setText("Saved");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(label6, gbc);
    savedLength = new JTextField();
    savedLength.setColumns(8);
    savedLength.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(savedLength, gbc);
    save = new JButton();
    save.setText("Save");
    controls.add(save);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(save, gbc);
    revert = new JButton();
    revert.setText("Revert");
    controls.add(revert);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(revert, gbc);
    clear = new JButton();
    clear.setText("Clear");
    controls.add(clear);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(clear, gbc);
    final JPanel spacer9 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer9, gbc);
    final JPanel spacer10 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer10, gbc);
    final JPanel spacer11 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer11, gbc);
    final JPanel spacer12 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer12, gbc);
    final JPanel spacer13 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer13, gbc);
    final JPanel spacer14 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer14, gbc);
    final JLabel label7 = new JLabel();
    label7.setHorizontalAlignment(SwingConstants.CENTER);
    label7.setHorizontalTextPosition(SwingConstants.CENTER);
    label7.setText("Length (px)");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 2;
    this.add(label7, gbc);
  }

  protected void wireUI() {
    save.addActionListener(this::save);
    revert.addActionListener(this::revert);
    clear.addActionListener(this::clear);
    measurementSelector.addItemListener(this);
  }

  @Override
  protected void save(ActionEvent e) {
    if (currentLine != null) {
      dataStore.set_endpoints(
          lastActiveImage.getMostRecentImageName(),
          (String) measurementSelector.getSelectedItem(),
          currentLine[0],
          currentLine[1]);
      savedLine = currentLine.clone();
    } else {
      dataStore.set_endpoints(
          lastActiveImage.getMostRecentImageName(),
          (String) measurementSelector.getSelectedItem(),
          null,
          null);
      savedLine = null;
    }
    dataStore.insert_value(
        lastActiveImage.getMostRecentImageName(),
        (String) measurementSelector.getSelectedItem(),
        currentMagnitude);

    savedMagnitude =
        (Double)
            dataStore.get_value(
                lastActiveImage.getMostRecentImageName(),
                (String) measurementSelector.getSelectedItem());
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
  public void updateInterface() {
    if (savedMagnitude != null) {
      savedLength.setText(String.format("%.3f", savedMagnitude));
    } else {
      savedLength.setText("");
    }

    if (currentMagnitude != null) {
      currentLength.setText(String.format("%.3f", currentMagnitude));
    } else {
      currentLength.setText("");
    }

    ImagePlus img = lastActiveImage.getMostRecentImageWindow();
    Roi active_roi = img.getRoi();
    if (active_roi == null && currentLine != null) {
      img.setRoi(new Line(currentLine[0].x, currentLine[0].y, currentLine[1].x, currentLine[1].y));
      //      img.setRoi(new PointRoi(currentPosition.x, currentPosition.y));
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
      Double saved_length =
          (Double)
              dataStore.get_value(lastActiveImage.getMostRecentImageName(), currentMeasurement);
    }
    updateUI();
  }

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e
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

      Rectangle bounds = roi.getBounds();
      if (currentLine == null) {
        currentLine = new Point2D.Double[2];
        currentLine[0] = new Point2D.Double();
        currentLine[1] = new Point2D.Double();
      }
      currentLine[0].setLocation(roi.x1, roi.y1);
      currentLine[1].setLocation(roi.x2, roi.y2);
      currentMagnitude = roi.getLength();
    }
    updateInterface();
  }
}
