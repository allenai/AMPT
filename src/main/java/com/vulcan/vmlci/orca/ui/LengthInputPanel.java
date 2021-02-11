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

import com.vulcan.vmlci.orca.ColumnDescriptor;
import com.vulcan.vmlci.orca.DataStore;
import com.vulcan.vmlci.orca.LastActiveImage;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class LengthInputPanel extends JPanel implements ActiveImageListener {
  DataStore dataStore;
  LastActiveImage lastActiveImage;
  private JCheckBox enableOverlays;
  private JComboBox<String> measurementSelector;
  private JTextField currentLength;
  private JTextField savedLength;
  private JButton save;
  private JButton revert;
  private JButton clear;
  private ArrayList<JComponent> controls;
  private Point2D.Double[] saved_endpoints = null;

  public LengthInputPanel(DataStore dataStore) {
    this.dataStore = dataStore;
    controls = new ArrayList<>();
    buildUI();
    wireUI();
    lastActiveImage = LastActiveImage.getInstance();
    lastActiveImage.addActiveImageListener(this);
  }

  private void buildUI() {
    List<ColumnDescriptor> descriptors =
        dataStore.descriptors.values().stream()
            .filter(s -> s.measurement_type.equals("length"))
            .collect(Collectors.toList());
    descriptors.sort((o1, o2) -> Integer.compare(o1.index, o2.index));
    Vector<String> measurments = new Vector<>();
    descriptors.forEach(e -> measurments.add(e.name));
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
    measurementSelector = new JComboBox<>(measurments);
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

  private void wireUI() {
    save.addActionListener(
        e -> {
          String measurement = measurementSelector.getActionCommand();
        });
  }

  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    boolean measuring = !evt.getNewImage().equals(LastActiveImage.NO_OPEN_IMAGE);

    controls.forEach(component -> component.setEnabled(measuring));
    if (measuring) {
      String currentMeasurement = (String) measurementSelector.getSelectedItem();
      saved_endpoints =
          dataStore.getEndpoints(lastActiveImage.getMost_recent_image(), currentMeasurement);
      Double saved_length = (Double) dataStore.get_value(lastActiveImage.getMost_recent_image(), currentMeasurement);

      if(saved_length != null){
        savedLength.setText(saved_length.toString());
      } else {
        savedLength.setText("");
      }
    }
  }
}
