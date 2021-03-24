/*
 *  Copyright (c) 2021 Vulcan Inc. All rights reserved.
 *  Licensed under the Apache 2.0 license. See LICENSE file in the project root for full license information.
 */

package com.vulcan.vmlci.orca;

import com.vulcan.vmlci.orca.ui.ControlWindow;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.SwingUtilities;

/** Simple test driver for exercising UI */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>AMPT")
public class AMPT_Main implements Command {

  @Parameter Logger logger;

  @Parameter
  private Context ctx;

  private static ControlWindow controlWindow = null;

  @Override
  public void run() {
    logger.info("Starting AMPT");
    SwingUtilities.invokeLater(
        () -> {
          if (null == AMPT_Main.controlWindow) {
            AMPT_Main.controlWindow = new ControlWindow(ctx);
          }
          AMPT_Main.controlWindow.setVisible(true);
        });
  }
}
