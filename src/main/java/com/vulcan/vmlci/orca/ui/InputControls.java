/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.ui;

import com.vulcan.vmlci.orca.data.DataStore;
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;
import ij.IJ;
import ij.ImagePlus;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.ArrayList;

public class InputControls implements ActiveImageListener {
  DataStore dataStore;
  CueManager cueManager;
  String activeImage;
  JPanel inputPanel;
  ArrayList<JComponent> controls;

  public InputControls(DataStore dataStore, CueManager cueManager) {
    this.dataStore = dataStore;
    this.cueManager = cueManager;

    buildUI();
    wireUI();
    LastActiveImage lastActiveImage = LastActiveImage.getInstance();
    this.activeImage = lastActiveImage.getMostRecentImageName();
    lastActiveImage.addActiveImageListener(this);
    lastActiveImage.fireImageChange(
        lastActiveImage.getMostRecentImageName(), lastActiveImage.getMostRecentImageName());
  }

  private void buildUI() {
    controls = new ArrayList<>();
    inputPanel = new JPanel();
    inputPanel.setLayout(new BorderLayout(0, 0));
    JTabbedPane tabbedPane = new JTabbedPane();
    inputPanel.add(tabbedPane, BorderLayout.CENTER);

    tabbedPane.addTab("Comments", new CommentInputPanel(dataStore, cueManager));
    tabbedPane.addTab("Reference Points", new PointInputPanel(dataStore, cueManager));
    tabbedPane.addTab("Lengths", new LengthInputPanel(dataStore, cueManager));

    tabbedPane.addChangeListener(
        e -> {
          ImagePlus img = LastActiveImage.getInstance().getMostRecentImageWindow();
          if (null != img) {
            img.deleteRoi();
          }
          InputPanel active_control =
              ((InputPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex()));
          switch (tabbedPane.getSelectedIndex()) {
            case 0:
              break;
            case 1:
              IJ.setTool("point");
              break;
            case 2:
              IJ.setTool("line");
              break;
          }
          if (null != img) {
            active_control.reload_fields();
          }
        });

    //    ((InputPanel)tabbedPane.getComponentAt(tabbedPane.getSelectedIndex())).updateInterface();

  }

  private void wireUI() {}

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
