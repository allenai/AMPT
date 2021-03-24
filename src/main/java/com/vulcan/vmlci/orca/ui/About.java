/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.ui;

import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class About extends JDialog {
  private static final String COPYRIGHT_2021_VULCAN_INC = "Copyright 2021 Vulcan Inc.";
  final Logger logger = new StderrLogService();
  private JPanel contentPane;
  private JButton buttonDismiss;
  private JTextPane licensePanel;

  public About(){
    this(null);
  }
  public About(Frame owner){
    super(owner);
    buildUI();
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonDismiss);
    pack();
    setSize(new Dimension(640, 480));
    wireUI();
  }

  private void buildUI() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridBagLayout());
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridBagLayout());
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    contentPane.add(panel1, gbc);
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    panel1.add(panel2, gbc);
    buttonDismiss = new JButton();
    buttonDismiss.setText("Dismiss");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel2.add(buttonDismiss, gbc);
    final JPanel spacer1 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel1.add(spacer1, gbc);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.ipadx = 1;
    gbc.ipady = 1;
    contentPane.add(panel3, gbc);
    final JScrollPane scrollPane1 = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel3.add(scrollPane1, gbc);
    licensePanel = new JTextPane();
    licensePanel.setContentType("text/html");
    licensePanel.setEditable(false);
    licensePanel.setEnabled(true);
    licensePanel.setText(getLicenseText());
    licensePanel.putClientProperty("html.disable", Boolean.FALSE);
    scrollPane1.setViewportView(licensePanel);
    final JPanel spacer2 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel3.add(spacer2, gbc);
    final JPanel spacer3 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.VERTICAL;
    panel3.add(spacer3, gbc);
    final JPanel spacer4 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    contentPane.add(spacer4, gbc);
    final JPanel spacer5 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    contentPane.add(spacer5, gbc);
  }

  private void wireUI() {
    buttonDismiss.addActionListener(e -> onClose());

    // call onClose() when cross is clicked
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new AboutWindowAdapter());

    // call onClose() on ESCAPE
    contentPane.registerKeyboardAction(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onClose();
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private String getLicenseText() {
    String license = About.COPYRIGHT_2021_VULCAN_INC;
    ;
    ClassLoader classLoader = getClass().getClassLoader();
    final InputStream licenseInput = classLoader.getResourceAsStream("documentation/license.html");
    if (null != licenseInput) {
      try (final BufferedReader licenseReader =
          new BufferedReader(new InputStreamReader(licenseInput))) {
        license = licenseReader.lines().collect(Collectors.joining());
        licenseInput.close();
      } catch (IOException e) {
        logger.error(e);
      }
    } else {
      logger.error("Could not find license.html in resource bundle.");
    }
    return license;
  }

  private void onClose() {
    // add your code here
    dispose();
  }
  private class AboutWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      onClose();
    }
  }
}
