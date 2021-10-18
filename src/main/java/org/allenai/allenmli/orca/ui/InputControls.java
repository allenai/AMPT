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
