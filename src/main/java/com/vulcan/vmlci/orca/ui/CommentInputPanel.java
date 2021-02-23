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

import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CommentInputPanel extends InputPanel {
  private JTextArea commentField;
  private JButton saveComments;
  private boolean comments_dirty = false;

  public CommentInputPanel(DataStore dataStore) {
    super(dataStore);
  }

  protected void buildUI() {
    this.setLayout(new BorderLayout(0, 0));
    commentField = new JTextArea();
    this.add(commentField, BorderLayout.CENTER);
    controls.add(commentField);
    saveComments = new JButton();
    saveComments.setText("Save Comments");
    controls.add(saveComments);
    this.add(saveComments, BorderLayout.SOUTH);
  }

  protected void wireUI() {
    saveComments.addActionListener(this::save);
    commentField.addKeyListener(
        new KeyAdapter() {
          /**
           * Invoked when a key has been typed. This event occurs when a key press is followed by a
           * key release.
           *
           * @param e
           */
          @Override
          public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            comments_dirty = true;
            updateInterface();
          }
        });
  }

  @Override
  public void updateInterface() {
    super.updateInterface();
    saveComments.setEnabled(comments_dirty);
  }

  @Override
  protected void save(ActionEvent e) {
    String text_to_save = commentField.getText();
    if (text_to_save.isEmpty()) {
      dataStore.insert_value(lastActiveImage.getMostRecentImageName(), "MEAS COMMENTS", null);
    } else {
      dataStore.insert_value(
          lastActiveImage.getMostRecentImageName(), "MEAS COMMENTS", text_to_save);
    }
    comments_dirty = false;
    updateInterface();
  }

  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    super.activeImageChanged(evt);
    String img = lastActiveImage.getMostRecentImageName();
    if (img.equals(LastActiveImage.NO_OPEN_IMAGE)) {
      commentField.setText("");
    } else {
      String saved_comments =
          (String)
              dataStore.get_value(lastActiveImage.getMostRecentImageName(), "MEAS COMMENTS", "");
      commentField.setText(saved_comments);
    }
    comments_dirty = false;
    updateInterface();
  }

}
