package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.boundary.login.RoleSelectionUI;
import com.fitwell.boundary.reports.InventoryReportForm;
import com.fitwell.boundary.reports.ReportParametersForm;
import com.fitwell.control.AuthenticationController;
import com.fitwell.control.TrainingClassController;

public class ConsultantDashboardUI extends JFrame {

	private JButton btnEmergencyToggle;
	private Timer uiUpdateTimer;
	private AuthenticationController authenticationController = AuthenticationController.getInstance();

	public ConsultantDashboardUI(int consultantId) {

		setTitle("FitWell – Consultant Dashboard");
		setSize(900, 580);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setContentPane(new AppBackgroundPanel());
		setLayout(new BorderLayout());

		String firstName = authenticationController.getConsultantFirstName(consultantId);

		JLabel welcome = UIBuilder.LabelBuilder.of("Welcome, " + firstName).horizontalAlignment(SwingConstants.CENTER)
				.font(new Font("Segoe UI", Font.BOLD, 32)).foreground(new Color(30, 55, 100)).build();

		JLabel subtitle = UIBuilder.LabelBuilder.of("Manage classes, equipment and reports")
				.horizontalAlignment(SwingConstants.CENTER).font(new Font("Segoe UI", Font.PLAIN, 16))
				.foreground(new Color(100, 100, 100)).build();

		JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
		header.setBorder(BorderFactory.createEmptyBorder(30, 10, 15, 10));
		header.setOpaque(false);
		header.add(welcome);
		header.add(subtitle);

		JButton btnUsageReport = createCardButton("Reports");
		JButton btnAddClass = createCardButton("Add Class");
		JButton btnEditClass = createCardButton("Edit Class");
		JButton btnEquipment = createCardButton( "Inventory");
		JButton btnPlans = createCardButton( "Training Plans");

		JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
		topRow.setOpaque(false);
		topRow.add(btnUsageReport);
		topRow.add(btnAddClass);
		topRow.add(btnEditClass);

		JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
		bottomRow.setOpaque(false);
		bottomRow.add(btnEquipment);
		bottomRow.add(btnPlans);
		
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.setOpaque(false);
		center.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		center.add(topRow);
		center.add(bottomRow);

		
		btnEmergencyToggle = UIBuilder.ButtonBuilder.of("").font(new Font("Segoe UI", Font.BOLD, 17))
			.focus(false).opaque(true).build();

		updateEmergencyButtonState();

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 45, 25, 45));
		bottomPanel.setOpaque(false);

		JButton btnLogout = UIBuilder.ButtonBuilder.of("Logout")
		.font(new Font("Segoe UI", Font.BOLD, 14)).background(Color.red)
		.foreground(Color.WHITE).focus(false).cursor(new Cursor(Cursor.HAND_CURSOR))
		.border(BorderFactory.createEmptyBorder(10, 20, 10, 20))
			.build();

		btnLogout.addActionListener(e -> {
			if (uiUpdateTimer != null)
				uiUpdateTimer.stop();
			new RoleSelectionUI().setVisible(true);
			dispose();
		});

		JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		logoutWrapper.setOpaque(false);
		logoutWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); 
		logoutWrapper.add(btnLogout);

		bottomPanel.add(logoutWrapper, BorderLayout.WEST);
		bottomPanel.add(btnEmergencyToggle, BorderLayout.CENTER);

		btnUsageReport.addActionListener(e -> new ReportParametersForm(this).setVisible(true));
		btnAddClass.addActionListener(e -> new TrainingClassForm().setVisible(true));
		btnEditClass.addActionListener(e -> new EditTrainingClassListForm(this).setVisible(true));

		btnEquipment.addActionListener(e -> {
			Object[] options = { "Manage Stock", "XML Report" };
			int choice = JOptionPane.showOptionDialog(this, "Choose an action for Equipment Inventory:",
					"Inventory Management", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
					options[0]);

			if (choice == 0) {
				ManageStockUI dlg = new ManageStockUI(this);
				dlg.setLocationRelativeTo(this);
				dlg.setVisible(true);
			} else if (choice == 1) {
				new InventoryReportForm().setVisible(true);
			}
		});

		btnPlans.addActionListener(e -> {
			PlansMenuUI dlg = new PlansMenuUI(this);
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
		});

		btnEmergencyToggle.addActionListener(e -> handleEmergencyClick());

		add(header, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		uiUpdateTimer = new Timer(1000, e -> {
			updateEmergencyButtonState();
			TrainingClassController.getInstance().autoUpdateClassStatuses();
		});
		uiUpdateTimer.start();
	}

	private void handleEmergencyClick() {
		boolean isActive = TrainingClassController.getInstance().isEmergencyActive();

		if (!isActive) {
			int confirm = JOptionPane.showConfirmDialog(this,
					"ACTIVATE EMERGENCY MODE?\nThis will PAUSE all active classes immediately.",
					"Emergency Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if (confirm == JOptionPane.OK_OPTION) {
				TrainingClassController.getInstance().startEmergencyMode();
				updateEmergencyButtonState();
			}

		} else {
			int confirm = JOptionPane.showConfirmDialog(this, "End emergency mode and RESUME all classes?",
					"Restore Routine", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (confirm == JOptionPane.OK_OPTION) {
				TrainingClassController.getInstance().endEmergencyMode();
				updateEmergencyButtonState();
			}
		}
	}

	private void updateEmergencyButtonState() {
		boolean isActive = TrainingClassController.getInstance().isEmergencyActive();
		LocalDateTime targetTime = TrainingClassController.getInstance().getEmergencyTargetTime();

		if (isActive && targetTime != null) {
			Duration remaining = Duration.between(LocalDateTime.now(), targetTime);
			long seconds = remaining.getSeconds();
			if (seconds < 0)
				seconds = 0;
			long mm = seconds / 60;
			long ss = seconds % 60;
			String timeStr = String.format("%02d:%02d", mm, ss);

			btnEmergencyToggle.setText("END EMERGENCY (Auto resume: " + timeStr + ")");
			btnEmergencyToggle.setBackground(new Color(34, 139, 34));
			btnEmergencyToggle.setForeground(Color.WHITE);
			btnEmergencyToggle.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0), 3));
		} else {
			btnEmergencyToggle.setText("ALERT - ACTIVATE EMERGENCY MODE");
			btnEmergencyToggle.setBackground(new Color(190, 40, 40));
			btnEmergencyToggle.setForeground(Color.WHITE);
			btnEmergencyToggle.setBorder(BorderFactory.createLineBorder(new Color(120, 20, 20), 2));
		}
	}


	private JButton createCardButton(String title) {

		JButton btn = UIBuilder.ButtonBuilder.of(title).background(new Color(240, 248, 255))
				.focus(false).opaque(true)
				.cursor(new Cursor(Cursor.HAND_CURSOR)).preferredSize(new Dimension(210, 140))
				.border(BorderFactory.createCompoundBorder(new LineBorder(new Color(210, 220, 235), 1),
						BorderFactory.createEmptyBorder(15, 15, 15, 15)))
				.font(new Font("Segoe UI Emoji", Font.BOLD, 24))
				.build();

		return btn;
	}

	private static class AppBackgroundPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			GradientPaint gp = new GradientPaint(0, 0, new Color(248, 250, 255), 0, getHeight(),
					new Color(225, 235, 245));
			g2.setPaint(gp);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}