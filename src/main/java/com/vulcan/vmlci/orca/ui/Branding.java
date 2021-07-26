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

package com.vulcan.vmlci.orca.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The <code>Branding</code> class is part of the UI that identifies the plugin and manages the
 * hamburger menus.
 */
public class Branding extends JPanel {

  JLabel title;
  JPopupMenu menu;
  JButton burgerButton;

  public Branding() {
    buildUI();
    wireUI();
  }

  private void buildUI() {
    GridBagConstraints gbc;
    setLayout(new GridBagLayout());
    title = new JLabel("Aquatic Mammal Photogrammetry Tool");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    add(title, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    final Icon hamburger =
        new ImageIcon(AccordionPanel.class.getResource("/images/hamburger_32.png"));
    burgerButton = new JButton(hamburger);
    burgerButton.setComponentPopupMenu(buildMenu());
    add(burgerButton, gbc);
  }

  private void wireUI() {
    burgerButton.addActionListener(
        e -> {
          menu.show(burgerButton, burgerButton.getWidth() / 2, burgerButton.getHeight() / 2);
        });
  }

  private JPopupMenu buildMenu() {
    menu = new JPopupMenu();
    JMenuItem menuItem = new JMenuItem("About");
    menuItem.addActionListener(
        e -> {
          final About dialog = new About((Frame) getTopLevelAncestor());
          dialog.setLocationByPlatform(true);
          dialog.setLocationRelativeTo(getTopLevelAncestor());
          dialog.setVisible(true);
        });
    menu.add(menuItem);
    menuItem = new JMenuItem("User Guide");
    menu.add(menuItem);
    menuItem.addActionListener(e -> new HelpLauncher((JFrame) getTopLevelAncestor()));
    menu.addSeparator();
    menuItem = new JMenuItem("Import Configuration");
    menu.add(menuItem);
    menuItem.addActionListener(
        e -> new ConfigurationImportLauncher((JFrame) getTopLevelAncestor()));
    menuItem = new JMenuItem("Export Configuration");
    menu.add(menuItem);
    menuItem.addActionListener(
        e -> new ConfigurationExportLauncher((JFrame) getTopLevelAncestor()));
    menuItem = new JMenuItem("Restore Default Configuration");
    menu.add(menuItem);
    menuItem.addActionListener(
        e -> new ConfigurationRestoreLauncher((JFrame) getTopLevelAncestor()));
    return menu;
  }
}
