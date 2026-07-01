package com.fitwell.boundary.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.boundary.consultant.ConsultantDashboardUI;
import com.fitwell.control.AuthenticationController;

public class ConsultantLoginUI extends JFrame {

    private JTextField idField;
    private JButton btnLogin;
    private AuthenticationController authenticationController = AuthenticationController.getInstance();

    public ConsultantLoginUI() {

        setTitle("FitWell – Consultant Login");
        setSize(550, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new AppBackgroundPanel());

        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0); // Spacing between rows
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++;
        JLabel title = UIBuilder.LabelBuilder.of("Consultant Login")
        		.horizontalAlignment(SwingConstants.CENTER)
        		.font(new Font("Segoe UI", Font.BOLD, 26)).foreground(new Color(30, 55, 100))
        		.build();

        mainPanel.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = UIBuilder.LabelBuilder.of("Please enter your ID to continue")
        		.horizontalAlignment(SwingConstants.CENTER)
        		.font(new Font("Segoe UI", Font.PLAIN, 14)).foreground(new Color(100, 100, 100))
        		.build();

        mainPanel.add(subtitle, gbc);

        gbc.gridy++;
        mainPanel.add(Box.createVerticalStrut(20), gbc);

        gbc.gridy++;
        JLabel lblId = UIBuilder.LabelBuilder.of("Consultant ID:")
        		.font(new Font("Segoe UI", Font.BOLD, 14)).foreground(new Color(80, 80, 80))
        		.build();
        		
        JPanel lblWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblWrapper.setBackground(Color.WHITE);
        lblWrapper.add(lblId);
        mainPanel.add(lblWrapper, gbc);

        gbc.gridy++;
        idField = new JTextField(15);
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        idField.setPreferredSize(new Dimension(250, 40)); // Comfortable height
        idField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Enter key performs login
        idField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
        mainPanel.add(idField, gbc);

        gbc.gridy++;
        mainPanel.add(Box.createVerticalStrut(20), gbc);

        gbc.gridy++;
        btnLogin = UIBuilder.ButtonBuilder.of("Login")
    			.font(new Font("Segoe UI", Font.BOLD, 16)).background(new Color(47, 93, 170))
    			.foreground(Color.WHITE).focus(false).cursor(new Cursor(Cursor.HAND_CURSOR))
    			.preferredSize(new Dimension(250, 45)).border(BorderFactory.createEmptyBorder(5, 15, 5, 15))
    			.build();

        btnLogin.addActionListener(e -> login());
        mainPanel.add(btnLogin, gbc);

        gbc.gridy++;
        btnLogin = UIBuilder.ButtonBuilder.of("Login")
    			.font(new Font("Segoe UI", Font.BOLD, 16)).background(new Color(47, 93, 170))
    			.foreground(Color.WHITE).focus(false).cursor(new Cursor(Cursor.HAND_CURSOR))
    			.preferredSize(new Dimension(250, 45)).border(BorderFactory.createEmptyBorder(5, 15, 5, 15))
    			.build();
        JButton btnBack = UIBuilder.ButtonBuilder.of("Back").font(new Font("Segoe UI", Font.PLAIN, 14))
        	.background(Color.WHITE).foreground(Color.GRAY).focus(false)
        	.cursor(new Cursor(Cursor.HAND_CURSOR)).preferredSize(new Dimension(250, 35))
        	.border(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)).build();
        
        btnBack.addActionListener(e -> {
            new RoleSelectionUI().setVisible(true);
            dispose();
        });
        mainPanel.add(btnBack, gbc);
        add(mainPanel);
    }

    private void login() {
        String text = idField.getText().trim();
        if (text.isEmpty()) {
            showError("Please enter  ID.");
            return;
        }
        if(text.length() > 8) {
            showError("ID must have 8 digits");
            return;
        }
        Integer id;
        try {
            id = Integer.parseInt(text);
        } catch (Exception e) {
            showError("ID must be a number.");
            return;
        }

        boolean ok = authenticationController.consultantExists(id);

        if (!ok) {
            showError("Consultant not found in the system.");
            return;
        }

        new ConsultantDashboardUI(id).setVisible(true);
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this,msg,"Login Error",JOptionPane.ERROR_MESSAGE);
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