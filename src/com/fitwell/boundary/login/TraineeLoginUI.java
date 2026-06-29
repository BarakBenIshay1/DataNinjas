package com.fitwell.boundary.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import com.fitwell.boundary.TraineeDashboardUI;
import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.AuthenticationController;

public class TraineeLoginUI extends JFrame {

	private JTextField traineeIdField;
	private JButton btnLogin;

	public TraineeLoginUI() {
		setTitle("FitWell - Trainee Login");
		setSize(450, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// General Background
		setContentPane(new AppBackgroundPanel());
		setLayout(new GridBagLayout());


		JLabel lblTitle = UIBuilder.LabelBuilder.of("Welcome Back").font(new Font("Segoe UI", Font.BOLD, 24))
				.foreground(new Color(30, 55, 100)).alignmentX(Component.CENTER_ALIGNMENT).build();

		JLabel lblSubtitle = UIBuilder.LabelBuilder.of("Enter your Trainee ID to access portal")
				.font(new Font("Segoe UI", Font.PLAIN, 13)).foreground(Color.GRAY)
				.alignmentX(Component.CENTER_ALIGNMENT).build();

		// 4. Styled Text Field
		JPanel fieldWrapper = UIBuilder.PanelBuilder.of(new BorderLayout())
				.background(Color.WHITE).maximumSize(new Dimension(300, 40))
				.alignmentX(Component.CENTER_ALIGNMENT).build();


		traineeIdField = new JTextField();
		styleTextField(traineeIdField);
		// Support Enter to login
		traineeIdField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					login();
			}
		});
		fieldWrapper.add(traineeIdField, BorderLayout.CENTER);

		// 5. Login Button
		btnLogin = new JButton("Login");
		stylePrimaryButton(btnLogin);
		btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnLogin.setMaximumSize(new Dimension(300, 45));
		btnLogin.addActionListener(e -> login());

		// 6. Back Button
		JButton btnBack = UIBuilder.ButtonBuilder.of("Back").font(new Font("Segoe UI", Font.PLAIN, 14))
				.background(Color.WHITE).foreground(Color.GRAY).focus(false).cursor(new Cursor(Cursor.HAND_CURSOR))
				.alignmentX(Component.CENTER_ALIGNMENT).border(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1))
				.build();
		btnBack.setMaximumSize(new Dimension(300, 35));

		btnBack.addActionListener(e -> {
			new RoleSelectionUI().setVisible(true);
			dispose();
		});

		// === White Card in Center ===
		JPanel cardPanel = UIBuilder.PanelBuilder.of().boxLayout(BoxLayout.Y_AXIS).background(Color.WHITE)
				.border(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1),
						new EmptyBorder(40, 40, 40, 40)))
				.add(Box.createVerticalStrut(10)).add(lblTitle).add(Box.createVerticalStrut(5))
				.add(lblSubtitle).add(Box.createVerticalStrut(30)).build();

		// Small label above field
		JLabel lblFieldTitle = UIBuilder.LabelBuilder.of("Trainee ID")
				.font(new Font("Segoe UI", Font.BOLD, 12)).foreground(new Color(80, 80, 80))
				.alignmentX(Component.CENTER_ALIGNMENT)
				.build();

		JPanel labelHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelHolder.setBackground(Color.WHITE);
		labelHolder.setMaximumSize(new Dimension(300, 20));
		labelHolder.add(lblFieldTitle);

		cardPanel.add(labelHolder);
		cardPanel.add(Box.createVerticalStrut(5));
		cardPanel.add(fieldWrapper);
		cardPanel.add(Box.createVerticalStrut(25));
		cardPanel.add(btnLogin);
		cardPanel.add(Box.createVerticalStrut(10));
		cardPanel.add(btnBack);

		add(cardPanel);
	}

	private void login() {
		String traineeIdStr = traineeIdField.getText().trim();

		if (traineeIdStr.isEmpty()) {
			showError("Trainee ID is required.");
			return;
		}

		int traineeId;
		try {
			traineeId = Integer.parseInt(traineeIdStr);
		} catch (NumberFormatException e) {
			showError("Trainee ID must be a valid number.");
			return;
		}

		try {
			boolean exists = AuthenticationController.getInstance().traineeExists(traineeId);
			if (!exists) {
				showError("Trainee ID not found or account is currently frozen.");
				return;
			}

			new TraineeDashboardUI(traineeIdStr).setVisible(true);
			dispose();

		} catch (Exception e) {
			showError("Database connection error.");
			e.printStackTrace();
		}
	}

	private void showError(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Login Error", JOptionPane.ERROR_MESSAGE);
	}


	private void styleTextField(JTextField txt) {
		txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1),
				new EmptyBorder(8, 10, 8, 10)));
	}

	private void stylePrimaryButton(JButton btn) {
		btn = UIBuilder.ButtonBuilder.of(btn).font(new Font("Segoe UI", Font.BOLD, 16))
				.background(new Color(30, 135, 30)).foreground(Color.WHITE).focus(false)
				.border(BorderFactory.createEmptyBorder(10, 20, 10, 20)).cursor(new Cursor(Cursor.HAND_CURSOR)).build();
	}

	private static class AppBackgroundPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(),
					new Color(225, 235, 250));
			g2.setPaint(gp);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}