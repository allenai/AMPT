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

import com.vulcan.vmlci.orca.helpers.ConfigurationLoader;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class HelpLauncher {
  final Logger logger;
  final ClassLoader classLoader;
  private final Path documentationRoot;
  private final ArrayList<String> documentationFiles;

  /** Construct a HelpLauncher. */
  public HelpLauncher() {
    this(null);
  }

  /**
   * Prepares the documentation for display.
   *
   * <p>This class copies the documentation from the resources into a subdirectory in the
   * preferences directory. This allows the user to have a persistent bookmark to the documentation.
   *
   * @param owner the window and dialogs are associated with
   */
  public HelpLauncher(JFrame owner) {
    logger = new StderrLogService();
    classLoader = HelpLauncher.class.getClassLoader();
    documentationFiles = new ArrayList<>();

    documentationRoot = ConfigurationLoader.getAbsoluteConfigurationPath("documentation");
    try {
      Files.createDirectories(documentationRoot);
    } catch (final IOException e) {
      logger.error("Problem creating documentation directory", e);
      return;
    }
    extractDocumentationManifest();
    populateDocumentationDirectory();
    launchBrowser(owner);
  }

  /** Loads the manifest file for the documentation files.s */
  private void extractDocumentationManifest() {

    final InputStream documentationManifest =
        classLoader.getResourceAsStream("documentation/manifest.txt");
    if (null != documentationManifest) {
      try (final BufferedReader licenseReader =
          new BufferedReader(new InputStreamReader(documentationManifest))) {
        Arrays.stream(licenseReader.lines().toArray())
            .forEach(line -> documentationFiles.add((String) line));
        documentationManifest.close();
      } catch (final IOException e) {
        logger.error(e);
      }
    } else {
      logger.error("documentation/manifest.txt missing");
    }
  }

  /**
   * Copies the documentation into the documentation directory.
   *
   * <p>TODO: Currently this copies the documentation each time help is invoked, which should be
   * changed.
   */
  private void populateDocumentationDirectory() {
    for (final String filename : documentationFiles) {
      final InputStream documentationFileStream =
          classLoader.getResourceAsStream("documentation/" + filename);
      if (null == documentationFileStream) {
        logger.error("Missing 'documentation/" + filename + "' resource");
        continue;
      }
      final Path destination = Paths.get(documentationRoot.toString(), filename);
      try {
        if (Files.exists(destination)) {
          Files.delete(destination);
        }
        Files.copy(documentationFileStream, destination);
      } catch (final IOException e) {
        logger.error(e);
      }
    }
  }

  /**
   * Attempts to launch the default web browser to show the documentation.
   *
   * @param owner passed to JOptionPane to support modal behavior. May be null.
   */
  private void launchBrowser(JFrame owner) {
    final Path indexPath = Paths.get(documentationRoot.toString(), "index.html");
    if (Files.notExists(indexPath)) {
      JOptionPane.showMessageDialog(
          owner, "Documentation not found", "Documentation Missing", JOptionPane.ERROR_MESSAGE);
      logger.error("Documentation not installed");
      return;
    }
    final URI documentationURI = indexPath.toUri();
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(documentationURI);
      } catch (final IOException e) {
        logger.error(e);
      }
    } else {
      JOptionPane.showMessageDialog(
          owner,
          "Browse to " + documentationURI,
          "Documentation Can't Open",
          JOptionPane.INFORMATION_MESSAGE);
      logger.info("Access documentation at " + documentationURI);
    }
  }

  public static void main(String[] args) {
    final HelpLauncher helpLauncher = new HelpLauncher(null);
  }
}
