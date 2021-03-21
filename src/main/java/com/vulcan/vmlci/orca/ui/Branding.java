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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The <code>Branding</code> class is part of the UI that identifies the plug and manages the about
 * an help menus.
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
    menuItem.addActionListener(e->new HelpLauncher((JFrame) getTopLevelAncestor()));
    return menu;
  }
}
