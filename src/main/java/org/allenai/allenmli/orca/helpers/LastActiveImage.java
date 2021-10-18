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

package org.allenai.allenmli.orca.helpers;

import org.allenai.allenmli.orca.event.ActiveImageChangeEvent;
import org.allenai.allenmli.orca.event.ActiveImageListener;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;

import javax.swing.event.EventListenerList;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Class to manage tracking of the image currently being measured.
 *
 * <p>This class emits {@link ActiveImageChangeEvent}s when the front-most ImagePlus window changes.
 */
public final class LastActiveImage implements PropertyChangeListener, ImageListener {
  public static final String NO_OPEN_IMAGE = "No Open Image";
  private static LastActiveImage instance;
  private final EventListenerList listenerList = new EventListenerList();
  private String most_recent_image;

  /** Constructs a LastActiveImage instance. */
  private LastActiveImage() {
    KeyboardFocusManager keyboardFocusManager =
        KeyboardFocusManager.getCurrentKeyboardFocusManager();
    keyboardFocusManager.addPropertyChangeListener("focusedWindow", this);
    ImagePlus.addImageListener(this);
    if (0 == WindowManager.getImageCount()) {
      most_recent_image = LastActiveImage.NO_OPEN_IMAGE;
    } else {
      most_recent_image = WindowManager.getCurrentImage().getTitle();
    }
  }

  /**
   * Factory method to construct/return a LastActiveImage
   *
   * @return reference to a LastActiveImage
   */
  public static LastActiveImage getInstance() {
    if (null == LastActiveImage.instance) {
      LastActiveImage.instance = new LastActiveImage();
    }
    return LastActiveImage.instance;
  }

  /**
   * Get the current ImagePlus name
   *
   * @return the last focused ImagePlus name.
   */
  public String getMostRecentImageName() {
    return most_recent_image;
  }

  /**
   * Get the current ImagePlus
   *
   * @return the last focused ImagePlus name.
   */
  public ImagePlus getMostRecentImageWindow() {
    return WindowManager.getImage(most_recent_image);
  }

  /**
   * Register an event listener.
   *
   * @param l instance of <code>ActiveImageListener</code>
   */
  public void addActiveImageListener(ActiveImageListener l) {
    listenerList.add(ActiveImageListener.class, l);
  }

  /**
   * Remove an event listener.
   *
   * @param l instance of <code>ActiveImageListener</code>
   */
  public void removeActiveImageListener(ActiveImageListener l) {
    listenerList.remove(ActiveImageListener.class, l);
  }

  /**
   * This method gets called when a bound property is changed.
   *
   * @param evt A PropertyChangeEvent object describing the event source and the property that has
   *     changed.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object newWindow = ((KeyboardFocusManager) evt.getSource()).getFocusedWindow();
    final String oldImage = this.most_recent_image;
    if (newWindow instanceof ImageWindow) {
      String image = ((ImageWindow) newWindow).getImagePlus().getTitle();
      if (!image.equals(most_recent_image)) {
        most_recent_image = image;
        this.fireImageChange(oldImage, most_recent_image);
      }
    }
    if (0 == WindowManager.getImageCount()) {
      most_recent_image = LastActiveImage.NO_OPEN_IMAGE;
      this.fireImageChange(oldImage, most_recent_image);
    }
  }

  /**
   * Cause an ActiveImageChangeEvent to be sent to listeners.
   *
   * @param oldImage the previous image name
   * @param newImage the new image name.
   */
  public void fireImageChange(String oldImage, String newImage) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    ActiveImageChangeEvent imageChangeEvent = new ActiveImageChangeEvent(this, oldImage, newImage);

    for (int i = 0; i < listeners.length - 1; i += 2) {
      if (ActiveImageListener.class == listeners[i]) {
        ((ActiveImageListener) listeners[i + 1]).activeImageChanged(imageChangeEvent);
      }
    }
  }

  @Override
  public void imageOpened(ImagePlus imp) {}

  /**
   * Fires an ActiveImageChangeEvent whenever an ImagePlus is closed. Most useful when there isn't
   * another ImagePlus that immediately takes focus.
   *
   * @param imp the ImagePlus that is closing
   */
  @Override
  public void imageClosed(ImagePlus imp) {
    String closedImage = imp.getTitle();
    if (0 == WindowManager.getImageCount()) {
      most_recent_image = LastActiveImage.NO_OPEN_IMAGE;
      this.fireImageChange(closedImage, most_recent_image);
    } else {
      fireImageChange(closedImage, WindowManager.getCurrentImage().getTitle());
    }
  }

  @Override
  public void imageUpdated(ImagePlus imp) {}

  public boolean no_images() {
    return (0 == WindowManager.getImageCount());
  }
}
