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
import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;
import com.vulcan.vmlci.orca.helpers.LastActiveImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/** Subclass of JPanel that has an associated cue manager, datastore, and last active image. */
public abstract class InputPanel extends JPanel implements ActiveImageListener, TableModelListener {
  protected final CueManager cueManager;
  protected final DataStore dataStore;
  protected final LastActiveImage lastActiveImage;

  /** Collection of the JComponents whose states are dependent on there being an open image. */
  protected final ArrayList<JComponent> controls;

  public InputPanel(DataStore dataStore, CueManager cueManager) {
    this.dataStore = dataStore;
    this.cueManager = cueManager;
    this.lastActiveImage = LastActiveImage.getInstance();
    this.controls = new ArrayList<>();
    this.lastActiveImage.addActiveImageListener(this);
    this.dataStore.addTableModelListener(this);
    buildUI();
    wireUI();
  }

  /** Build the user interface. Called during instance construction. */
  protected abstract void buildUI();

  /** Wire the event handlers. Called during instance construction. */
  protected abstract void wireUI();

  /**
   * Perform the actions required to save a measurement
   *
   * @param e the event the triggers the save action
   */
  protected void save(ActionEvent e) {}

  /**
   * Perform the actions required to revert a measurement
   *
   * @param e the event the triggers the revert action
   */
  protected void revert(ActionEvent e) {}

  /**
   * Perform the actions required to clear a measurement
   *
   * @param e the event the triggers the clear action
   */
  protected void clear(ActionEvent e) {}

  /**
   * Perform the actions required to approve a measurement
   *
   * @param e the event the triggers the approve action
   */
  protected void approve(ActionEvent e) {}

  /** Re-rerender the UI */
  public void updateInterface() {}

  /**
   * Manage changes to system state when the active image changes.
   *
   * @param evt the ActiveImageChangeEvent
   */
  @Override
  public void activeImageChanged(ActiveImageChangeEvent evt) {
    boolean new_state = !evt.getNewImage().equals(LastActiveImage.NO_OPEN_IMAGE);
    controls.forEach(component -> component.setEnabled(new_state));
    reload_fields();
  }

  /** Reloads the class's state fields with new values. */
  public void reload_fields() {}

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e indicate what has changed in the table.
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (e.getType() == TableModelEvent.UPDATE
        && e.getLastRow() >= ((DataStore) e.getSource()).getRowCount()) {
      reload_fields();
    }
  }

  /**
   * Makes the component visible or invisible. Also sets activeCue in the CueManager to null.
   * subclasses should override this if they have associated cues.
   *
   * <p>Overrides <code>Component.setVisible</code>.
   *
   * @param aFlag true to make the component visible; false to make it invisible
   */
  @Override
  public void setVisible(boolean aFlag) {
    if (aFlag) {
      cueManager.setActiveCue(null);
    }
    super.setVisible(aFlag);
    cueManager.draw();
  }
}
