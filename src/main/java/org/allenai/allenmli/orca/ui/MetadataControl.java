/*
 *  Copyright (c) 2021 Vulcan Inc.
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
import org.allenai.allenmli.orca.event.ActiveImageChangeEvent;
import org.allenai.allenmli.orca.event.ActiveImageListener;
import org.allenai.allenmli.orca.helpers.LastActiveImage;

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

  public MetadataControl(DataStore dataStore, CueManager cueManager) {
    super(dataStore, cueManager);
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
    this.add(new JLabel("Animal ID"), gbc);

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

    final DirtyMetadata dirtyMetadata = new DirtyMetadata();
    whaleIDField.addKeyListener(dirtyMetadata);
    positionField.addKeyListener(dirtyMetadata);

    updateMetadata.addActionListener(this::save);
  }

  @Override
  protected void save(ActionEvent e) {
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
    if (null == selection) {
      dataStore.insert_value(filename, "UNDERWATER", null);
    } else {
      dataStore.insert_value(filename, "UNDERWATER", selection.getActionCommand());
    }
    dirty = false;
    updateInterface();
  }

  /**
   * Perform the actions required to revert a measurement
   *
   * @param e the event the triggers the revert action
   */
  @Override
  protected void revert(ActionEvent e) {

  }

  /**
   * Perform the actions required to clear a measurement
   *
   * @param e the event the triggers the clear action
   */
  @Override
  protected void clear(ActionEvent e) {

  }

  /**
   * Perform the actions required to approve a measurement
   *
   * @param e the event the triggers the approve action
   */
  @Override
  protected void approve(ActionEvent e) {

  }

  @Override
  public void updateInterface() {
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

  /**
   * Reloads the class's state fields with new values.
   */
  @Override
  public void reload_fields() {

  }

  private class DirtyMetadata extends KeyAdapter {
    /**
     * Invoked when a key has been typed. This event occurs when a key press is followed by a key
     * release.
     *
     * @param e
     */
    @Override
    public void keyTyped(KeyEvent e) {
      super.keyTyped(e);
      dirty = true;
      updateInterface();
    }
  }
}
