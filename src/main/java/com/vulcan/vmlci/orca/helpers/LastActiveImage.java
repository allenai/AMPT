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

package com.vulcan.vmlci.orca.helpers;

import com.vulcan.vmlci.orca.event.ActiveImageChangeEvent;
import com.vulcan.vmlci.orca.event.ActiveImageListener;
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
public class LastActiveImage implements PropertyChangeListener, ImageListener {
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
    if(WindowManager.getImageCount() == 0){
      most_recent_image = NO_OPEN_IMAGE;
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
    if (instance == null) {
      instance = new LastActiveImage();
    }
    return instance;
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
    if (WindowManager.getImageCount() == 0) {
      most_recent_image = NO_OPEN_IMAGE;
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
      if (listeners[i] == ActiveImageListener.class) {
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
    if (WindowManager.getImageCount() == 0) {
      most_recent_image = NO_OPEN_IMAGE;
      this.fireImageChange(closedImage, most_recent_image);
    } else {
      fireImageChange(closedImage, WindowManager.getCurrentImage().getTitle());
    }
  }

  @Override
  public void imageUpdated(ImagePlus imp) {}

  public boolean no_images(){
    return (WindowManager.getImageCount() == 0);
  }
}
