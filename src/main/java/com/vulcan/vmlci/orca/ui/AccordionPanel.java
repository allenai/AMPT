/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class AccordionPanel extends JPanel {
  private JPanel content_panel;
  private JCheckBox controlButton;

  /**
   * Creates a new <code>AccordionPanel</code>
   *
   * @param title the value used to label the <code>AccordionPanel</code>.
   * @param start_open the initial display state.
   */
  public AccordionPanel(String title, boolean start_open) {
    this.build_ui(title, start_open);
  }

  private void build_ui(String title, boolean initial_state) {
    Icon left_arrow = new ImageIcon(AccordionPanel.class.getResource("/images/arrow_left.png"));
    Icon down_arrow = new ImageIcon(AccordionPanel.class.getResource("/images/arrow_down.png"));
    controlButton = new JCheckBox(left_arrow);
    controlButton.setSelectedIcon(down_arrow);
    controlButton.setSelected(initial_state);

    this.setLayout(new GridBagLayout());
    JPanel title_panel = new JPanel(new BorderLayout());
    title_panel.add(new JLabel(title), BorderLayout.CENTER);
    title_panel.add(controlButton, BorderLayout.EAST);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(title_panel, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    this.content_panel = new JPanel();
    this.content_panel.setVisible(true);
    this.add(this.content_panel, gbc);

    // Register event handler for button
    controlButton.addActionListener(
        e -> {
          content_panel.setVisible(controlButton.isSelected());
          content_panel.invalidate();
        });
  }

  /**
   * Gets the content_panels.
   *
   * @return The current content panel
   */
  public JPanel getContent_panel() {
    return content_panel;
  }

  /**
   * Replaces the current content panel
   *
   * @param content_panel - A replacement JPanel instance.
   */
  public void setContent_panel(JPanel content_panel) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    this.remove(this.content_panel);

    this.content_panel = content_panel;
    this.add(this.content_panel, gbc);
//    content_panel.invalidate();
    this.invalidate();
  }
}
