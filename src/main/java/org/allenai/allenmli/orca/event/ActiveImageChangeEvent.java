/*
 *  Copyright (c) 2021 The Allen Institute for Artificial Intelligence.
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

package org.allenai.allenmli.orca.event;

import org.allenai.allenmli.orca.helpers.LastActiveImage;

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
