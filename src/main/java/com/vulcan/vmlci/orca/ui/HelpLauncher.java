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
