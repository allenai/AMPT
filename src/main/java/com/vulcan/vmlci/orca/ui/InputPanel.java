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
  protected abstract void save(ActionEvent e);

  /**
   * Perform the actions required to revert a measurement
   *
   * @param e the event the triggers the revert action
   */
  protected abstract void revert(ActionEvent e);

  /**
   * Perform the actions required to clear a measurement
   *
   * @param e the event the triggers the clear action
   */
  protected abstract void clear(ActionEvent e);

  /**
   * Perform the actions required to approve a measurement
   *
   * @param e the event the triggers the approve action
   */
  protected abstract void approve(ActionEvent e);

  /** Re-rerender the UI */
  public abstract void updateInterface();

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
  public abstract void reload_fields();

  /**
   * This fine grain notification tells listeners the exact range of cells, rows, or columns that
   * changed.
   *
   * @param e indicate what has changed in the table.
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (TableModelEvent.UPDATE == e.getType()
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
