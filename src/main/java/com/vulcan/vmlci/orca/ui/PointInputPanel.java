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
import java.util.ArrayList;
import java.util.List;
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
  private ArrayList<JComponent> controls;


  public PointInputPanel(DataStore dataStore) {
    super(dataStore);
    PointRoi.addRoiListener(this);
  }

  private void buildUI() {
    List<ColumnDescriptor> descriptors =
            dataStore.descriptors.values().stream()
                    .filter(s -> s.measurement_type.equals("point") && s.name.endsWith("_x"))
                    .collect(Collectors.toList());
    descriptors.sort((o1, o2) -> Integer.compare(o1.index, o2.index));
    Vector<String> measurements = new Vector<>();
    descriptors.forEach(e -> measurements.add(e.name.substring(0,e.name.length()-2)));

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
    measurementSelector = new JComboBox<String>(measurements);
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
  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    boolean new_state = !evt.getNewImage().equals(LastActiveImage.NO_OPEN_IMAGE);
    controls.forEach(component -> component.setEnabled(new_state));
  }
}
