package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.PlanController;
import com.fitwell.control.PlanController.PlanStatus;
import com.fitwell.control.PlanController.PlanView;

public class ManagePlansUI extends JDialog {

    private JComboBox<PlanView> cmbPlans;
    private JComboBox<PlanStatus> cmbStatus;
    private JLabel lblType;

    private PlanController controller = PlanController.getInstance();

    public ManagePlansUI(JFrame parent) {
        super(parent, "FitWell - Manage Plans", true);

        setSize(700, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(new AppBackgroundPanel());
        setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 35, 25, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Manage Plans", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(30, 55, 100));
        mainPanel.add(lblTitle, gbc);

        gbc.gridy++;
        JLabel lblSub = new JLabel("Update status and manage group assignments", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(80, 80, 80));
        mainPanel.add(lblSub, gbc);

        // Plan selector
        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 8, 0);
        mainPanel.add(createStyledLabel("Plan:"), gbc);

        gbc.gridy++;
        cmbPlans = new JComboBox<>();
        styleComboBox(cmbPlans);
        mainPanel.add(cmbPlans, gbc);

        // Type + Status row
        gbc.gridy++;
        gbc.insets = new Insets(14, 0, 0, 0);
        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setOpaque(false);

        JPanel pnlType = new JPanel(new BorderLayout());
        pnlType.setOpaque(false);
        pnlType.add(createStyledLabel("Type:"), BorderLayout.NORTH);
        lblType = new JLabel("-");
        lblType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblType.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        pnlType.add(lblType, BorderLayout.CENTER);

        JPanel pnlStatus = new JPanel(new BorderLayout());
        pnlStatus.setOpaque(false);
        pnlStatus.add(createStyledLabel("Status:"), BorderLayout.NORTH);
        cmbStatus = new JComboBox<>();
        controller.getPlanStatusList().forEach(cmbStatus::addItem);
        styleComboBox(cmbStatus);
        pnlStatus.add(cmbStatus, BorderLayout.CENTER);

        row.add(pnlType);
        row.add(pnlStatus);
        mainPanel.add(row, gbc);

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 15, 0));
        btnRow.setOpaque(false);

        JButton btnMembers = createSecondaryButton("👥 Manage Members (Group only)");
        JButton btnSave = createPrimaryButton("Save Status");

        btnRow.add(btnMembers);
        btnRow.add(btnSave);
        mainPanel.add(btnRow, gbc);

        // Close
        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        JButton btnClose = createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        mainPanel.add(btnClose, gbc);

        add(mainPanel);

        // Load data
        loadPlans();
        onPlanChanged();

        // Listeners
        cmbPlans.addActionListener(e -> onPlanChanged());

        btnSave.addActionListener(e -> onSave());

        btnMembers.addActionListener(e -> onMembers(parent));
    }

    private void loadPlans() {
        cmbPlans.removeAllItems();
        List<PlanView> plans = controller.getAllPlansForDropdown();
        for (PlanView p : plans) cmbPlans.addItem(p);
    }

    private void onPlanChanged() {
        if (cmbPlans.getSelectedItem() == null) {
            lblType.setText("-");
            cmbStatus.setSelectedItem("Active");
            return;
        }

        PlanView info = (PlanView)cmbPlans.getSelectedItem();

        lblType.setText(info.getType());
        cmbStatus.setSelectedItem(info.getStatus());
    }

    private void onSave() {
        try {
            if (cmbPlans.getSelectedItem() == null) return;

            PlanView info = (PlanView)cmbPlans.getSelectedItem();

            PlanStatus newStatus = (PlanStatus)cmbStatus.getSelectedItem();
            if(newStatus == null){
                JOptionPane.showMessageDialog(this, "please select a status");
            }

            boolean ok = controller.updatePlanStatus(info.getPlanId(), newStatus.getPlanStatusId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Status updated");
                loadPlans();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onMembers(JFrame parent) {
        if (cmbPlans.getSelectedItem() == null) return;

        PlanView info = (PlanView)cmbPlans.getSelectedItem();
        if (!"Group".equalsIgnoreCase(info.getType())) {
            JOptionPane.showMessageDialog(this, "Members are only relevant for Group plans.");
            return;
        }

        new GroupPlanMembersUI(parent, info.getPlanId()).setVisible(true);
    }

    // ===== STYLE HELPERS =====

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(new Color(200, 200, 200)));
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = UIBuilder.ButtonBuilder.of(text).focus(false)
        .font(new Font("Segoe UI", Font.PLAIN, 14))
        .background(new Color(230, 240, 255)).foreground(new Color(30, 55, 100))
        .border(BorderFactory.createLineBorder(new Color(180, 200, 230)))
        .cursor(new Cursor(Cursor.HAND_CURSOR)).build();
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = UIBuilder.ButtonBuilder.of(text)
        .focus(false).font(new Font("Segoe UI", Font.BOLD, 14))
        .background(new Color(35, 135, 55)).foreground(Color.WHITE)
        .border(BorderFactory.createEmptyBorder(12, 0, 12, 0))
        .cursor(new Cursor(Cursor.HAND_CURSOR)).build();
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