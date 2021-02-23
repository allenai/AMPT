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
import com.vulcan.vmlci.orca.event.ActiveImageListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.stream.Stream;

public class MetadataControl extends InputPanel implements ActiveImageListener {
  ButtonGroup underwaterGroup;
  private JTextField filenameField;
  private JTextField whaleIDField;
  private JTextField positionField;
  private HashMap<String, JRadioButton> underwater;
  private JButton updateMetadata;
  private boolean dirty = false;

  public MetadataControl(DataStore dataStore) {
    super(dataStore);
  }

  @Override
  protected void buildUI() {
    GridBagConstraints gbc;
    updateMetadata = new JButton("Update Metadata");
    controls.add(updateMetadata);
    JPanel buttonPanel = new JPanel();
    underwaterGroup = new ButtonGroup();
    filenameField = new JTextField("");
    filenameField.setEditable(false);
    whaleIDField = new JTextField();
    controls.add(whaleIDField);
    positionField = new JTextField();
    controls.add(positionField);
    underwater = new HashMap<>();
    Stream.of("0", "1", "2")
        .forEach(
            checkbox_label -> {
              JRadioButton checkbox = new JRadioButton(checkbox_label, false);
              checkbox.setActionCommand(checkbox_label);
              underwater.put(checkbox_label, checkbox);
              buttonPanel.add(checkbox);
              underwaterGroup.add(checkbox);
              controls.add(checkbox);
            });

    buttonPanel.doLayout();
    this.setLayout(new GridBagLayout());

    // Filename Label and Field
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    JLabel filename = new JLabel("Filename");
    filename.setLabelFor(filenameField);
    this.add(filename, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(filenameField, gbc);

    // Labels
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(new JLabel("Whale ID"), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(new JLabel("Position"), gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(new JLabel("Underwater"), gbc);

    // Controls
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(whaleIDField, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(positionField, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 3;
    this.add(buttonPanel, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(updateMetadata, gbc);

    this.doLayout();
  }

  @Override
  protected void wireUI() {
    String activeImage = lastActiveImage.getMostRecentImageName();
    underwater
        .values()
        .forEach(
            radioButton ->
                radioButton.addActionListener(
                    e -> {
                      dirty = true;
                      updateInterface();
                    }));

    whaleIDField.addKeyListener(
        new KeyAdapter() {
          /**
           * Invoked when a key has been typed. This event occurs when a key press is followed by a
           * key release.
           *
           * @param e
           */
          @Override
          public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            dirty = true;
            updateInterface();
          }
        });

    positionField.addKeyListener(
        new KeyAdapter() {
          /**
           * Invoked when a key has been typed. This event occurs when a key press is followed by a
           * key release.
           *
           * @param e
           */
          @Override
          public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            dirty = true;
            updateInterface();
          }
        });

    updateMetadata.addActionListener(this::save);
  }

  @Override
  protected void save(ActionEvent e) {
    super.save(e);
    String filename = lastActiveImage.getMostRecentImageName();
    String whaleID = whaleIDField.getText();
    String position = positionField.getText();
    ButtonModel selection = underwaterGroup.getSelection();
    if (whaleID.isEmpty()) {
      whaleID = null;
    }
    dataStore.insert_value(filename, "WhaleID", whaleID);
    if (position.isEmpty()) {
      position = null;
    }
    dataStore.insert_value(filename, "Position", position);
    if (selection == null) {
      dataStore.insert_value(filename, "UNDERWATER", null);
    } else {
      dataStore.insert_value(filename, "UNDERWATER", selection.getActionCommand());
    }
    dirty = false;
    updateInterface();
  }

  @Override
  public void updateInterface() {
    super.updateInterface();
    updateMetadata.setEnabled(dirty);
  }

  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    //    underwaterGroup.clearSelection();
    super.activeImageChanged(evt);
    String activeImage = evt.getNewImage();
    filenameField.setText(activeImage);
    underwaterGroup.clearSelection();
    if (activeImage.equals(LastActiveImage.NO_OPEN_IMAGE)) {
      whaleIDField.setText("");
      positionField.setText("");
    } else {
      whaleIDField.setText((String) dataStore.get_value(activeImage, "WhaleID", ""));
      positionField.setText((String) dataStore.get_value(activeImage, "Position", ""));
      String underwaterCode = (String) dataStore.get_value(activeImage, "UNDERWATER", "");
      if (underwater.containsKey(underwaterCode)) {
        underwater.get(underwaterCode).setSelected(true);
      }
    }
    dirty = false;
    updateInterface();
  }
}
