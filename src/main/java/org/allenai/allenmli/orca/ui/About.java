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

import com.google.common.collect.ImmutableMap;
import org.allenai.allenmli.orca.helpers.Utilities;
import org.allenai.allenmli.orca.Version;
import org.apache.commons.text.StringSubstitutor;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class About extends JDialog {
  private static final String COPYRIGHT_ALLEN_AI =
      "Copyright 2021, 2022 The Allen Institute for Artificial Intelligence.";
  final Logger logger = new StderrLogService();
  private JPanel contentPane;
  private JButton buttonDismiss;
  private JTextPane textPanel;

  public About() {
    this(null);
  }

  public About(Frame owner) {
    super(owner);
    buildUI();
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonDismiss);
    pack();
    setSize(new Dimension(640, 480));
    wireUI();
  }

  private void buildUI() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridBagLayout());
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridBagLayout());
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    contentPane.add(panel1, gbc);
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    panel1.add(panel2, gbc);
    buttonDismiss = new JButton();
    buttonDismiss.setText("Dismiss");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel2.add(buttonDismiss, gbc);
    final JPanel spacer1 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel1.add(spacer1, gbc);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.ipadx = 1;
    gbc.ipady = 1;
    contentPane.add(panel3, gbc);
    final JScrollPane scrollPane1 = new JScrollPane();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel3.add(scrollPane1, gbc);
    textPanel = new JTextPane();
    textPanel.setContentType("text/html");
    textPanel.setEditable(false);
    textPanel.setEnabled(true);
    textPanel.setText(getText());
    textPanel.putClientProperty("html.disable", Boolean.FALSE);
    scrollPane1.setViewportView(textPanel);
    final JPanel spacer2 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel3.add(spacer2, gbc);
    final JPanel spacer3 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.VERTICAL;
    panel3.add(spacer3, gbc);
    final JPanel spacer4 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    contentPane.add(spacer4, gbc);
    final JPanel spacer5 = new JPanel();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    contentPane.add(spacer5, gbc);
  }

  private void wireUI() {
    buttonDismiss.addActionListener(e -> onClose());

    // call onClose() when cross is clicked
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new AboutWindowAdapter());

    // call onClose() on ESCAPE
    contentPane.registerKeyboardAction(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onClose();
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    // Manage URL Clicks
    textPanel.addHyperlinkListener(
        e -> {
          final HyperlinkEvent.EventType eventType = e.getEventType();
          if (HyperlinkEvent.EventType.ACTIVATED.equals(eventType)) {
            final URI documentationURI;
            try {
              documentationURI = e.getURL().toURI();
            } catch (final URISyntaxException ex) {
              logger.error(ex);
              return;
            }
            Utilities.linkOpener(this, documentationURI);
          }
        });
  }

  private String getText() {
    String text = About.COPYRIGHT_ALLEN_AI;
    final ClassLoader classLoader = getClass().getClassLoader();
    final InputStream aboutInput = classLoader.getResourceAsStream("documentation/about.xsl");
    final InputStream attributionInput = classLoader.getResourceAsStream("attribution.xml");
    if (null != aboutInput && null != attributionInput) {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      try {
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(attributionInput);

        final StreamSource styleSource = new StreamSource(aboutInput);
        final Transformer transformer = transformerFactory.newTransformer(styleSource);

        final DOMSource source = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);

        transformer.transform(source, result);
        text = writer.toString();
      } catch (final IOException
          | ParserConfigurationException
          | SAXException
          | TransformerException e) {
        logger.error(e);
      }
    } else {
      logger.error(
          "Resource bundle is corrupted: missing about.xsl and/or attribution.xml. Try reinstalling the plugin.");
    }
    final StringSubstitutor stringSubstitutor =
        new StringSubstitutor(ImmutableMap.of(
                "version", Version.VERSION,
                "copyright", Version.COPYRIGHT));
    text = stringSubstitutor.replace(text);
    return text;
  }

  private void onClose() {
    dispose();
  }

  private class AboutWindowAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      onClose();
    }
  }
}
