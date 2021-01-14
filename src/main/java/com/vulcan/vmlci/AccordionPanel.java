package com.vulcan.vmlci;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccordionPanel extends JPanel {
    private Icon left_arrow = null;
    private Icon down_arrow = null;
    private JPanel title_panel;
    private JPanel content_panel;
    private JCheckBox controlButton;

    /**
     * Creates a new <code>AccordianPanel</code>
     */
    public AccordionPanel(String title, boolean start_open) {
        this.build_ui(title, start_open);
    }

    private void build_ui(String title, boolean initial_state) {
        left_arrow = new ImageIcon(AccordionPanel.class.getResource("/images/arrow_left.png"));
        down_arrow = new ImageIcon(AccordionPanel.class.getResource("/images/arrow_down.png"));
        controlButton = new JCheckBox(left_arrow);
        controlButton.setSelectedIcon(down_arrow);
        controlButton.setSelected(initial_state);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.title_panel = new JPanel(new BorderLayout());
        this.title_panel.add(new JLabel(title), BorderLayout.CENTER);
        this.title_panel.add(controlButton, BorderLayout.EAST);

        this.add(this.title_panel);

        this.content_panel = new JPanel();
        this.content_panel.setVisible(true);
        this.add(this.content_panel);


        // Register event handler for button
        controlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                content_panel.setVisible(controlButton.isSelected());
                content_panel.invalidate();
            }
        });
    }


    /**
     * Gets the content_panels.
     * @return The current content panel
     */
    public JPanel getContent_panel() {
        return content_panel;
    }

    /**
     * Replaces the current content panel
     * @param content_panel
     */
    public void setContent_panel(JPanel content_panel) {
        this.content_panel = content_panel;
    }
}
