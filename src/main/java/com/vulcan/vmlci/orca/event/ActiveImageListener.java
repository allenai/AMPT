/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca.event;

import java.util.EventListener;

/**
 * Interface for an observer to register to receive notifications of a new ImagePlus taking focus.
 */
public interface ActiveImageListener extends EventListener {
  /**
   * Gives notification that an ImagePlus has taken focus.
   *
   * @param evt the ActiveImageChangeEvent
   */
  void activeImageChanged(ActiveImageChangeEvent evt);
}
