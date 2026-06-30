package com.fitwell.boundary.login;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.boundary.manager.ManagerDashboardUI;
import com.fitwell.control.ManagerController;

public class ManagerLoginUI extends JFrame {

    private JTextField idField;
    

    public ManagerLoginUI() {
        setTitle("FitWell - Manager Login");
        setSize(500, 450); // Increased height for back button
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Background
        setContentPane(new AppBackgroundPanel());
        setLayout(new GridBagLayout());

        // Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(40, 50, 40, 50)
        ));
        
        JLabel title = UIBuilder.LabelBuilder.of("Institute Manager").font(new Font("Segoe UI", Font.BOLD, 24))
        		.foreground(new Color(30, 55, 100)).build();
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Field
        JLabel lblId = new JLabel("Manager ID:");
        lblId.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        idField = new JTextField(15);
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        idField.setMaximumSize(new Dimension(250, 40));
        idField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) login(); }
        });

        JButton btnLogin = new JButton("Login");
        styleButton(btnLogin);
        btnLogin.addActionListener( (e) -> {
        	login();
        });
        
        // Back Button
        JButton btnBack = UIBuilder.ButtonBuilder.of("Back")
        	.font(new Font("Segoe UI", Font.PLAIN, 14)).background(Color.WHITE)
        	.foreground(Color.GRAY).focus(false).
        	border(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)).build();
        	
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(250, 35));

        btnBack.addActionListener(e -> {
            new RoleSelectionUI().setVisible(true);
            dispose();
        });

        card.add(title);
        card.add(Box.createVerticalStrut(30));
        card.add(lblId);
        card.add(idField);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnBack);

        add(card);
    }

    private void login() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            if (ManagerController.getInstance().isManagerExists(id)) {
                // To Manager Dashboard
                new ManagerDashboardUI().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Manager ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton btn) {
    	btn = UIBuilder.ButtonBuilder.of(btn).font(new Font("Segoe UI", Font.BOLD, 14))
    	.background(new Color(30, 55, 100)).foreground(Color.WHITE).focus(false)
    	.build();

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
    }

    // Background Class
    private static class AppBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, new Color(248, 250, 255), 0, getHeight(), new Color(225, 235, 245));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}