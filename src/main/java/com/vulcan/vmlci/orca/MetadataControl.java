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

package com.vulcan.vmlci.orca;

import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.stream.Stream;

public class MetadataControl implements ActiveImageListener {
  JPanel displayPanel;
  ButtonGroup underwaterGroup;
  String activeImage;
  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  private JTextField filenameField;

  private JTextField whaleIDField;
  private JTextField positionField;
  private HashMap<String, JRadioButton> underwater;
  private DataStore dataStore;

  public MetadataControl(DataStore dataStore) {
    this.dataStore = dataStore;
    build_ui();
    wire_ui();
    LastActiveImage.getInstance().addActiveImageListener(this);
    activeImage = LastActiveImage.getInstance().getMost_recent_image();
  }

  private void build_ui() {
    GridBagConstraints gbc;
    JPanel buttonPanel = new JPanel();
    underwaterGroup = new ButtonGroup();
    displayPanel = new JPanel();
    filenameField = new JTextField();
    whaleIDField = new JTextField();
    positionField = new JTextField();
    underwater = new HashMap<>();
    Stream.of("0", "1", "2")
        .forEach(
            checkbox_label -> {
              JRadioButton checkbox = new JRadioButton(checkbox_label, false);
              underwater.put(checkbox_label, checkbox);
              buttonPanel.add(checkbox);
              underwaterGroup.add(checkbox);
            });

    buttonPanel.doLayout();
    displayPanel.setLayout(new GridBagLayout());

    // Filename Label and Field
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    displayPanel.add(new JLabel("Filename"), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 3;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    displayPanel.add(filenameField, gbc);

    // Labels
    gbc = new GridBagConstraints();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    displayPanel.add(new JLabel("Whale ID"), gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    displayPanel.add(new JLabel("Position"), gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    displayPanel.add(new JLabel("Underwater"), gbc);

    // Controls
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    displayPanel.add(whaleIDField, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    displayPanel.add(positionField, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 3;
    displayPanel.add(buttonPanel, gbc);

    displayPanel.doLayout();
  }

  private void wire_ui() {
    whaleIDField.setActionCommand("WhaleID");
    whaleIDField.addActionListener(
        e -> {
          if (!activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
            dataStore.insert_value(activeImage, "WhaleID", ((JTextField) e.getSource()).getText());
          }
        });
    whaleIDField.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {}

          @Override
          public void focusLost(FocusEvent e) {
            if (!activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
              dataStore.insert_value(
                  activeImage, "WhaleID", ((JTextField) e.getSource()).getText());
            }
          }
        });
    positionField.setActionCommand("Position");
    positionField.addActionListener(
        e -> {
          if (!activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
            dataStore.insert_value(activeImage, "Position", ((JTextField) e.getSource()).getText());
          }
        });
    positionField.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {}

          @Override
          public void focusLost(FocusEvent e) {
            if (!activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
              dataStore.insert_value(
                  activeImage, "Position", ((JTextField) e.getSource()).getText());
            }
          }
        });

    underwater.forEach(
        (s, jCheckBox) -> {
          jCheckBox.setActionCommand(s);
          jCheckBox.setEnabled(false);
          jCheckBox.addActionListener(
              e -> {
                if (!activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
                  dataStore.insert_value(activeImage, "UNDERWATER", e.getActionCommand());
                }
              });
        });
  }

  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    activeImage = evt.getNewImage();
    if (activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
      filenameField.setText("");
      whaleIDField.setText("");
      whaleIDField.setEditable(false);
      positionField.setText("");
      positionField.setEditable(false);
      underwaterGroup.clearSelection();
      underwater.forEach(
          (s, jCheckBox) -> jCheckBox.setEnabled(false));
    } else {
      filenameField.setText(activeImage);
      String whaleID = (String) dataStore.get_value(activeImage, "WhaleID", "");
      String position = (String) dataStore.get_value(activeImage, "Position", "");
      String underwaterCode = (String) dataStore.get_value(activeImage, "UNDERWATER", "");
      whaleIDField.setText(whaleID);
      whaleIDField.setEditable(true);
      positionField.setText(position);
      positionField.setEditable(true);
      underwaterGroup.clearSelection();
      underwater.forEach(
          (s, jCheckBox) -> {
            jCheckBox.setEnabled(true);
            if (s.equals(underwaterCode)) {
              jCheckBox.setSelected(true);
            }
          });
    }
  }
}