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

package com.vulcan.vmlci.orca;

import com.vulcan.vmlci.orca.helpers.ConfigurationManager;
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

  @Parameter private Context ctx;

  private static ControlWindow controlWindow = null;

  @Override
  public void run() {
    logger.info("Starting AMPT");
    SwingUtilities.invokeLater(
        () -> {
          if (!ConfigurationManager.checkFormatVersions()) {
            return;
          }
          if (null == AMPT_Main.controlWindow) {
            AMPT_Main.controlWindow = new ControlWindow(ctx);
          }
          AMPT_Main.controlWindow.setVisible(true);
        });
  }
}
