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

package org.allenai.allenmli.orca.ui;

import org.allenai.allenmli.orca.helpers.ConfigurationLoader;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HelpLauncher {
  final Logger logger;
  final ClassLoader classLoader;
  private final Path documentationRoot;

  /** Construct a HelpLauncher. */
  public HelpLauncher() {
    this(null);
  }

  /**
   * Prepares the documentation for display.
   *
   * <p>This class copies the documentation from the resources into a subdirectory in the
   * preferences' directory. This allows the user to have a persistent bookmark to the documentation.
   *
   * @param owner the window and dialogs are associated with
   */
  public HelpLauncher(JFrame owner) {
    logger = new StderrLogService();
    classLoader = HelpLauncher.class.getClassLoader();

    documentationRoot = ConfigurationLoader.getAbsoluteConfigurationPath("documentation");

    try {
      unpackZip();
    } catch (final IOException e) {
      logger.error(e);
      return;
    }
    launchBrowser(owner);
  }

  /**
   * Unpacks the documentation zip file.
   *
   * @throws IOException if there is any problem unpacking the documentation.
   */
  private void unpackZip() throws IOException {
    final String outputPrefix = documentationRoot.toFile().getCanonicalFile().toString();
    final InputStream zipStream = classLoader.getResourceAsStream("documentation.zip");
    final ZipInputStream zipInputStream;
    if (null != zipStream) {
      zipInputStream = new ZipInputStream(zipStream);
    } else {
      throw new IOException("Cannot find documentation.zip");
    }

    ZipEntry zipEntry = zipInputStream.getNextEntry();
    final byte[] buffer = new byte[1024];

    while (null != zipEntry) {
      final java.io.File destPath =
          ConfigurationLoader.getAbsoluteConfigurationPath(zipEntry.getName())
              .toFile()
              .getCanonicalFile();

      // Avoid badness
      if (!destPath.toString().startsWith(outputPrefix)) {
        logger.error("Attempted to create a file outside of " + outputPrefix);
        throw new IOException("Illegal Directory Access");
      }

      if (zipEntry.isDirectory()) {
        if (!destPath.isDirectory() && !destPath.mkdirs()) {
          throw new IOException("Failed to create directory " + destPath);
        }
        //noinspection ResultOfMethodCallIgnored
        destPath.setLastModified(zipEntry.getTime());
      } else {
        // ensure directory exists. (guard against badly formed zip file)
        final java.io.File destinationDirectory = destPath.getParentFile();
        if (!destinationDirectory.isDirectory() && !destinationDirectory.mkdirs()) {
          throw new IOException("Failed to create directory " + destinationDirectory);
        }

        if (!destPath.exists() || destPath.lastModified() < zipEntry.getTime()) {
          logger.info("Creating: " + destPath);
          // write file content
          final FileOutputStream targetFile = new FileOutputStream(destPath);
          int len = zipInputStream.read(buffer);
          while (0 < len) {
            targetFile.write(buffer, 0, len);
            len = zipInputStream.read(buffer);
          }
          targetFile.close();
          //noinspection ResultOfMethodCallIgnored
          destPath.setLastModified(zipEntry.getTime());
        }
      }

      zipEntry = zipInputStream.getNextEntry();
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
