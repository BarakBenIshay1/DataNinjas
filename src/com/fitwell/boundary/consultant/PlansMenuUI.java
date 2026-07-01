package com.fitwell.boundary.consultant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;

public class PlansMenuUI extends JDialog {

    public PlansMenuUI(JFrame parent) {
        super(parent, "FitWell - Training Plans", true);

        setSize(550, 420);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(new AppBackgroundPanel());
        setLayout(new GridBagLayout()); 

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(35, 45, 35, 45) 
        ));

        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.gridy = 0;
        root.weightx = 1.0;
        root.weighty = 1.0;
        root.fill = GridBagConstraints.NONE;
        root.anchor = GridBagConstraints.CENTER;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Training Plans");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 55, 100));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, gbc);

        gbc.gridy++;
        JLabel lblSub = new JLabel("Create and manage Personal & Group Plans", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(100, 100, 100));
        mainPanel.add(lblSub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(25, 0, 10, 0); 

        JButton btnPersonal = createSecondaryButton("Create Personal Plan");
        JButton btnGroup = createSecondaryButton("Create Group Plan");
        JButton btnManage = createPrimaryButton("Manage Plans");

        btnPersonal.addActionListener(e -> new CreatePersonalPlanUI(parent).setVisible(true));
        btnGroup.addActionListener(e -> new CreateGroupPlanUI(parent).setVisible(true));
        btnManage.addActionListener(e -> new ManagePlansUI(parent).setVisible(true));

        mainPanel.add(btnPersonal, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 10, 0); 
        mainPanel.add(btnGroup, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0); 
        mainPanel.add(btnManage, gbc);

        add(mainPanel, root);
    }

    // ===== STYLE HELPERS =====

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);

        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btn.setBackground(new Color(240, 245, 255));
        btn.setForeground(new Color(30, 55, 100));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 200, 230)),
                new EmptyBorder(10, 0, 10, 0)
        ));
        btn.setPreferredSize(new Dimension(280, 45)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btn.setPreferredSize(new Dimension(280, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private static class AppBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(248, 250, 255),
                    0, getHeight(), new Color(225, 235, 245)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}