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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The <code>ControlWindow</code> class is the main UI for the Aquatic Mammal Photogrammetry Tool.
 */
public class ControlWindow implements ActiveImageListener {
  private final JPanel metadata = null;
  private final JPanel input = null;
  private final JPanel length_measurements = null;
  private final JPanel body_profiles = null;
  private JFrame application_frame = null;
  private JPanel toplevel = null;
  private JPanel branding = null;
  private JPanel csv_controls = null;
  private DataStore ds;
  private MetadataDisplay metadataDisplay;

  public ControlWindow() {
    try {
      ds = new DataStore();
    } catch (ConfigurationFileLoadException | DataFileLoadException e) {
      e.printStackTrace();
    }

    SwingUtilities.invokeLater(this::build_ui);
  }

  private void build_ui() {
    LastActiveImage.getInstance();
    metadataDisplay = new MetadataDisplay(ds);
    GridBagConstraints c = new GridBagConstraints();
    this.toplevel = new JPanel();
    this.toplevel.setLayout(new GridBagLayout());

    GridBagConstraints gbc;

    // Constraints for branding
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Branding
    this.branding = new Branding();
    this.toplevel.add(branding, gbc);

    // Center Space
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    this.toplevel.add(build_accordion(), gbc);

    // Data Controls
    this.csv_controls = new DataControls(ds);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.toplevel.add(csv_controls, gbc);

    this.application_frame = new JFrame();
    this.application_frame.setTitle("Test Window");
    this.application_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.application_frame.add(toplevel);
    this.application_frame.pack();
    this.application_frame.setVisible(true);

    LastActiveImage.getInstance().addActiveImageListener(this);
  }

  private JComponent build_accordion() {
    JPanel frame = new JPanel();
    JScrollPane scrollPane = new JScrollPane(frame);
    GridBagConstraints gbc = new GridBagConstraints();
    frame.setLayout(new GridBagLayout());

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 0;
    AccordionPanel metadata = new AccordionPanel("Metadata", true);
    metadata.getContent_panel().add(metadataDisplay.getContent_panel());
    frame.add(metadata, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 1;
    AccordionPanel demo2 = new AccordionPanel("Input", true);
    demo2.getContent_panel().add(new JLabel("Lorem Ipsum"));
    frame.add(demo2, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 2;
    AccordionPanel demo3 = new AccordionPanel("Length Measurements", true);
    demo3.getContent_panel().add(new JLabel("Lorem Ipsum"));
    frame.add(demo3, gbc);

    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 0;
    gbc.gridx = 0;
    gbc.gridy = 3;
    AccordionPanel demo4 = new AccordionPanel("Body Profiles", true);
    demo4.getContent_panel().add(new JLabel("Lorem Ipsum"));
    frame.add(demo4, gbc);

    final JPanel spacer = new JPanel();
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.VERTICAL;
    frame.add(spacer, gbc);
    frame.setBackground(Color.CYAN);
    return scrollPane;
  }


  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    application_frame.setTitle(String.format("Measuring: %s", evt.getNewImage()));
  }
}
