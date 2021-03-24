/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.event;

import com.vulcan.vmlci.orca.helpers.LastActiveImage;

import java.util.EventObject;

public class ActiveImageChangeEvent extends EventObject {
  private final String oldImage;
  private final String newImage;

  /**
   * Constructs an ActiveImageChange event.
   *
   * @param source The object on which the Event initially occurred.
   * @param oldImage The previously active image.
   * @param newImage The newly active image.
   * @throws IllegalArgumentException if source is null.
   */
  public ActiveImageChangeEvent(LastActiveImage source, String oldImage, String newImage) {
    super(source);
    this.oldImage = oldImage;
    this.newImage = newImage;
  }

  /**
   * Provides the previous image name
   *
   * @return filename associated with previous ImagePlus
   */
  public String getOldImage() {
    return oldImage;
  }

  /**
   * Provides the new image name
   *
   * @return filename associated with latest ImagePlus
   */
  public String getNewImage() {
    return newImage;
  }
}
