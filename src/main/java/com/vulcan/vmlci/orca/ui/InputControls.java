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
import ij.IJ;
import ij.ImagePlus;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.ArrayList;

public class InputControls implements ActiveImageListener {
  DataStore dataStore;
  String activeImage;
  JPanel inputPanel;
  ArrayList<JComponent> controls;

  public InputControls(DataStore dataStore) {
    this.dataStore = dataStore;
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

    tabbedPane.addTab("Comments", new CommentInputPanel(dataStore));
    tabbedPane.addTab("Points", new PointInputPanel(dataStore));
    tabbedPane.addTab("Lengths", new LengthInputPanel(dataStore));

    tabbedPane.addChangeListener(
        e -> {
          ImagePlus img = LastActiveImage.getInstance().getMostRecentImageWindow();
          if (img != null) {
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
          if (img != null) {
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
