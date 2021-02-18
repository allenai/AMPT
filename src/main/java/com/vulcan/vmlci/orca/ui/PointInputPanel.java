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

import com.vulcan.vmlci.orca.DataStore;
import com.vulcan.vmlci.orca.LastActiveImage;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import ij.ImagePlus;
import ij.gui.PointRoi;
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

public class PointInputPanel extends InputPanel implements RoiListener, ItemListener {

  // UI Elements
  private JComboBox<String> measurementSelector;
  private JTextField currentPointX;
  private JTextField currentPointY;
  private JTextField savedPointY;
  private JTextField savedPointX;
  private JButton save;
  private JButton revert;
  private JButton clear;
  private JCheckBox enableOverlays;

  // State Elements
  private Point2D.Double currentPosition = null;
  private Point2D.Double savedPosition = null;

  public PointInputPanel(DataStore dataStore) {
    super(dataStore);
    PointRoi.addRoiListener(this);
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      lastActiveImage.getMostRecentImageWindow().deleteRoi();
      reload_fields();
    }
  }

  @Override
  public void reload_fields() {
    savedPosition =
        dataStore.get_point(
            lastActiveImage.getMostRecentImageName(),
            (String) measurementSelector.getSelectedItem());
    if (savedPosition != null) {
      currentPosition = (Point2D.Double) savedPosition.clone();
    } else {
      currentPosition = null;
    }
    updateInterface();
  }

  protected void buildUI() {
    Vector<String> measurements =
        dataStore.descriptors.values().stream() // Give me a stream of descriptors
            .filter( // Grab the relevant descriptors
                s ->
                    s.measurement_type.equals("point") // We need points
                        && s.name.endsWith("_x")) // but only the x coordinate
            .sorted(Comparator.comparingInt(o -> o.index)) // Sort, using index field
            .map(s -> s.name.substring(0, s.name.length() - 2)) // Get part of the descriptor name
            .collect(Collectors.toCollection(Vector::new)); // Make a vector for JComboBox

    this.setLayout(new GridBagLayout());
    final JPanel spacer1 = new JPanel();
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.VERTICAL;
    this.add(spacer1, gbc);
    final JPanel spacer2 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer2, gbc);
    currentPointX = new JTextField();
    currentPointX.setColumns(8);
    currentPointX.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(currentPointX, gbc);
    measurementSelector = new JComboBox<>(measurements);
    controls.add(measurementSelector);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 9;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(measurementSelector, gbc);
    final JLabel label1 = new JLabel();
    label1.setHorizontalAlignment(SwingConstants.CENTER);
    label1.setHorizontalTextPosition(SwingConstants.CENTER);
    label1.setText("X");
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 3;
    this.add(label1, gbc);
    savedPointX = new JTextField();
    savedPointX.setColumns(8);
    savedPointX.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 4;
    gbc.gridy = 5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(savedPointX, gbc);
    final JPanel spacer3 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 5;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer3, gbc);
    currentPointY = new JTextField();
    currentPointY.setColumns(8);
    currentPointY.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(currentPointY, gbc);
    savedPointY = new JTextField();
    savedPointY.setColumns(8);
    savedPointY.setEditable(false);
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(savedPointY, gbc);
    final JLabel label2 = new JLabel();
    label2.setHorizontalAlignment(SwingConstants.CENTER);
    label2.setHorizontalTextPosition(SwingConstants.CENTER);
    label2.setText("Y");
    gbc = new GridBagConstraints();
    gbc.gridx = 6;
    gbc.gridy = 3;
    this.add(label2, gbc);
    final JPanel spacer4 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 7;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer4, gbc);
    save = new JButton();
    save.setText("Save");
    controls.add(save);
    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(save, gbc);
    revert = new JButton();
    revert.setText("Revert");
    controls.add(revert);
    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(revert, gbc);
    final JPanel spacer5 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 9;
    gbc.gridy = 4;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer5, gbc);
    final JPanel spacer6 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 10;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer6, gbc);
    clear = new JButton();
    clear.setText("Clear");
    controls.add(clear);
    gbc = new GridBagConstraints();
    gbc.gridx = 8;
    gbc.gridy = 6;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(clear, gbc);
    final JLabel label3 = new JLabel();
    label3.setText("Current");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(label3, gbc);
    final JLabel label4 = new JLabel();
    label4.setText("Saved");
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(label4, gbc);
    final JPanel spacer7 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 4;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer7, gbc);
    final JPanel spacer8 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(spacer8, gbc);
    enableOverlays = new JCheckBox();
    enableOverlays.setHorizontalAlignment(SwingConstants.CENTER);
    enableOverlays.setHorizontalTextPosition(SwingConstants.TRAILING);
    enableOverlays.setText("Overlays");
    controls.add(enableOverlays);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 9;
    this.add(enableOverlays, gbc);
  }

  @Override
  protected void wireUI() {
    save.addActionListener(this::save);
    revert.addActionListener(this::revert);
    clear.addActionListener(this::clear);
    measurementSelector.addItemListener(this);
  }

  @Override
  protected void save(ActionEvent e) {
    dataStore.set_point(
        lastActiveImage.getMostRecentImageName(),
        (String) measurementSelector.getSelectedItem(),
        currentPosition);
    savedPosition =
        dataStore.get_point(
            lastActiveImage.getMostRecentImageName(),
            (String) measurementSelector.getSelectedItem());
    updateInterface();
  }

  @Override
  protected void revert(ActionEvent e) {
    lastActiveImage.getMostRecentImageWindow().deleteRoi();
    currentPosition = (Point2D.Double) savedPosition.clone();
    updateInterface();
  }

  @Override
  public void updateInterface() {
    if (savedPosition != null) {
      savedPointX.setText(Integer.toString((int) savedPosition.x));
      savedPointY.setText(Integer.toString((int) savedPosition.y));
    } else {
      savedPointX.setText("");
      savedPointY.setText("");
    }

    if (currentPosition != null) {
      currentPointX.setText(Integer.toString((int) currentPosition.x));
      currentPointY.setText(Integer.toString((int) currentPosition.y));
    } else {
      currentPointX.setText("");
      currentPointY.setText("");
    }

    ImagePlus img = lastActiveImage.getMostRecentImageWindow();
    Roi active_roi = img.getRoi();
    if (active_roi == null && currentPosition != null) {
      img.setRoi(new PointRoi(currentPosition.x, currentPosition.y));
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
    if (lastActiveImage.getMostRecentImageName() != LastActiveImage.NO_OPEN_IMAGE) {
      updateInterface();
    } else {
      currentPointX.setText("");
      currentPointY.setText("");
      savedPointX.setText("");
      savedPointY.setText("");
    }
  }

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (e == null) {
      savedPosition = null;
    }
  }

  @Override
  protected void clear(ActionEvent e) {
    currentPosition = null;
    lastActiveImage.getMostRecentImageWindow().deleteRoi();
    updateInterface();
  }

  private void renderLandmark() {
    ImagePlus img = lastActiveImage.getMostRecentImageWindow();
    if (currentPointX.getText().isEmpty() || currentPointY.getText().isEmpty()) {
      img.deleteRoi();
    } else {
      img.setRoi(
          new PointRoi(
              Double.parseDouble(currentPointX.getText()),
              Double.parseDouble(currentPointY.getText())));
    }
  }

  @Override
  public void roiModified(ImagePlus imp, int id) {
    if (imp == null || !imp.getTitle().equals(lastActiveImage.getMostRecentImageName())) {
      return;
    }

    if (id == RoiListener.DELETED) {
      currentPosition = null;
    } else {
      Roi roi = imp.getRoi();
      if (!(roi instanceof PointRoi)) {
        return;
      }
      Rectangle bounds = roi.getBounds();
      if (currentPosition == null) {
        currentPosition = new Point2D.Double();
      }
      currentPosition.setLocation(bounds.x, bounds.y);
    }
    updateInterface();
  }
}
