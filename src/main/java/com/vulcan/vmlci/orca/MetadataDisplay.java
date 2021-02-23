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

import com.vulcan.vmlci.orca.data.ColumnDescriptor;
import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;
import com.vulcan.vmlci.orca.helpers.ConfigurationFileLoadException;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;

import static com.vulcan.vmlci.orca.helpers.ConfigurationLoader.get_csv_file;

@Deprecated
public class MetadataDisplay implements ActiveImageListener, ActionListener, FocusListener {
  private final LastActiveImage image_watcher = LastActiveImage.getInstance();
  private final DataStore ds;
  private final HashMap<String, JComponent> ui_components;
  private String active_file_name;
  private ArrayList<String> animal_ids;
  private TextEntryWindow text_editor;
  private JComponent content_panel;

  public MetadataDisplay(DataStore ds) {
    this.ds = ds;
    this.ui_components = new HashMap<>();
    for (final ColumnDescriptor columnDescriptor : ds.descriptors.values()) {
      if (columnDescriptor.is_metadata) {
        this.ui_components.put(columnDescriptor.name, null);
      }
    }
    this.active_file_name = LastActiveImage.NO_OPEN_IMAGE;

    this.loadAnimalIds();
    build_metadata_display();
    //    SwingUtilities.invokeLater(this::build_metadata_display);
  }

  /** Loads the list of known animal ids. */
  private void loadAnimalIds() {
    ArrayList<HashMap<String, String>> animal_id_config;
    try {
      animal_id_config = get_csv_file("csv-whaleid.csv");
    } catch (ConfigurationFileLoadException e) {
      e.printStackTrace();
      animal_id_config = new ArrayList<>();
    }
    animal_ids = new ArrayList<>();
    animal_ids.add("");
    for (HashMap<String, String> entry : animal_id_config) {
      animal_ids.add(entry.get("Animal ID"));
    }
  }

  private void build_metadata_display() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagLayout grid_bag_layout = new GridBagLayout();
    panel.setLayout(grid_bag_layout);
    GridBagConstraints grid_constraints = new GridBagConstraints();
    int v_gap = 5;
    int h_gap = 10;
    // top, left, bottom, right
    grid_constraints.insets = new Insets(v_gap, h_gap, v_gap, h_gap);
    grid_constraints.gridx = 0;
    grid_constraints.gridy = 0;
    grid_constraints.gridwidth = 1;
    grid_constraints.gridheight = 1;
    grid_constraints.weightx = 1;
    grid_constraints.fill = GridBagConstraints.HORIZONTAL;

    for (final ColumnDescriptor columnDescriptor : ds.descriptors.values()) {
      if (!columnDescriptor.is_metadata) {
        continue;
      }
      String col_name = columnDescriptor.name;
      String measurement_type = columnDescriptor.measurement_type;
      boolean editable = columnDescriptor.editable;

      if (measurement_type.equals("manual") && !editable) {
        JLabel label = new JLabel(col_name);
        JTextField component = new JTextField("", 30);
        component.setEditable(false);
        component.setName(col_name);
        add_to_panel(label, component, grid_bag_layout, grid_constraints, panel);
        grid_constraints.gridy += 1;
        this.ui_components.put(col_name, component);
      } else if (measurement_type.equals("manual")) {
        JLabel label = new JLabel(col_name);
        JTextField component = new JTextField("", 10);
        component.setEditable(true);
        component.setName(col_name);
        component.addActionListener(this);
        component.addFocusListener(this);
        add_to_panel(label, component, grid_bag_layout, grid_constraints, panel);
        grid_constraints.gridy += 1;
        this.ui_components.put(col_name, component);
      } else if (measurement_type.equals("selection")) {
        JLabel label = new JLabel(col_name);
        JComboBox<String> component = new JComboBox<>();
        for (String element : animal_ids) {
          component.addItem(element);
        }
        component.setSelectedIndex(0);
        component.setEditable(true);
        component.setName(col_name);
        add_to_panel(label, component, grid_bag_layout, grid_constraints, panel);
        grid_constraints.gridy += 1;
        this.ui_components.put(col_name, component);
        component.addActionListener(this);
      } else if (measurement_type.equals("free text")) {
        JLabel label = new JLabel(col_name);
        JButton component = new JButton("Edit");
        component.setName(col_name);
        add_to_panel(label, component, grid_bag_layout, grid_constraints, panel);
        grid_constraints.gridy += 1;
        this.ui_components.put(col_name, component);
        component.addActionListener(this);
      }
    }
    JScrollPane scroll_pane = new JScrollPane(panel);
    scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    this.content_panel = scroll_pane;
    scroll_pane.validate();
    LastActiveImage.getInstance().addActiveImageListener(this);
  }

  private void add_to_panel(
      JLabel label,
      JComponent component,
      GridBagLayout layout,
      GridBagConstraints constraints,
      JPanel panel) {
    if (label != null) {
      constraints.gridx = 0;
      constraints.anchor = GridBagConstraints.WEST;
      layout.setConstraints(label, constraints);
      panel.add(label);
    }
    if (component != null) {
      constraints.gridy += 1;
      constraints.gridx = 0;
      constraints.anchor = GridBagConstraints.WEST;
      layout.setConstraints(component, constraints);
      panel.add(component);
    }
  }

  public JComponent getContent_panel() {
    return content_panel;
  }

  /**
   * Receives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    active_file_name = evt.getNewImage();
    update_ui();
  }

  @SuppressWarnings("rawtypes")
  private void update_ui() {
    for (String key : ui_components.keySet()) {
      JComponent component = ui_components.get(key);
      String ds_value = ds.get_value(active_file_name, key, "").toString();
      if (component instanceof JTextField) {
        ((JTextField) component).setText(ds_value);
      } else if (component instanceof JComboBox) {
        if (active_file_name.equals(LastActiveImage.NO_OPEN_IMAGE)) {
          ((JComboBox) component).setSelectedItem(0);
        } else {
          ((JComboBox) component).setSelectedItem(ds_value);
        }
      }
    }
  }

  /**
   * Invoked when an action occurs.
   *
   * @param e a control was activated
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void actionPerformed(ActionEvent e) {
    JComponent event_source = (JComponent) e.getSource();
    if (active_file_name.equals(LastActiveImage.NO_OPEN_IMAGE)) {
      return;
    }
    if (event_source instanceof JTextField) {
      String value = ((JTextField) event_source).getText();
      ds.insert_value(active_file_name, event_source.getName(), value);
    } else if (event_source instanceof JComboBox) {
      String value = (String) ((JComboBox) event_source).getEditor().getItem();
      if (!animal_ids.contains(value)) {
        animal_ids.add(value);
        ((JComboBox) event_source).addItem(value);
      }
      ds.insert_value(active_file_name, event_source.getName(), value);
    } else if (event_source instanceof JButton) {
      if (text_editor == null) {
        text_editor = new TextEntryWindow(ds);
      }
      text_editor.frame.setVisible(true);
    }
  }

  /**
   * Invoked when a component gains the keyboard focus.
   *
   * @param e event for object gaining focus.
   */
  @Override
  public void focusGained(FocusEvent e) {}

  /**
   * Invoked when a component loses the keyboard focus.
   *
   * @param e event for object losing focus.
   */
  @Override
  public void focusLost(FocusEvent e) {
    JTextField event_source = (JTextField) e.getSource();
    if (active_file_name.equals(LastActiveImage.NO_OPEN_IMAGE)) {
      return;
    }

    String value = event_source.getText();
    ds.insert_value(active_file_name, event_source.getName(), value);
  }
}
