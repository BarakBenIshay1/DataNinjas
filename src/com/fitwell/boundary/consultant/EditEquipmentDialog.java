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
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.TrainingClassController;
import com.fitwell.control.TrainingClassController.EquipmentTypeView;

public class EditEquipmentDialog extends JDialog {

    private final Map<Integer, Integer> selectedEquipment = new HashMap<>();
    private final List<EquipmentRowPanel> rowPanels = new ArrayList<>();
    private boolean confirmed = false;

    private TrainingClassController controller = TrainingClassController.getInstance();

    public EditEquipmentDialog(Window owner, LocalDateTime start, LocalDateTime end, int classId, Map<Integer, Integer> currentEquipment) {
        super(owner, "Edit Class Equipment", ModalityType.APPLICATION_MODAL);
        setSize(550, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = UIBuilder.LabelBuilder.of("Edit Equipment")
            .font(new Font("Segoe UI", Font.BOLD, 22))
            .foreground(new Color(30, 55, 100)).build();

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        JLabel subtitle = UIBuilder.LabelBuilder.of("Availability for: " + start.format(timeFmt) + " - " + end.format(timeFmt))
            .font(new Font("Segoe UI", Font.PLAIN, 14))
            .foreground(new Color(80, 80, 80)).build();

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBorder(new EmptyBorder(10, 20, 10, 20));

        List<EquipmentTypeView> availabilityData = controller.getAvailabilityReportForTimeExcludingClass(start, end, classId);

        for (EquipmentTypeView eqType : availabilityData) {
            int typeId = eqType.getEquipmentTypeID();
            String name = eqType.getName();
            int available = eqType.getAvailable();
            
            int preselectedQty = currentEquipment.getOrDefault(typeId, 0);

            EquipmentRowPanel panel = new EquipmentRowPanel(typeId, name, available, preselectedQty);
            rowPanels.add(panel);
            listContainer.add(panel);
            listContainer.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(200, 60, 60));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Confirm Selection");
        styleButton(btnSave, new Color(34, 139, 34));
        btnSave.addActionListener(e -> {
            selectedEquipment.clear();
            for (EquipmentRowPanel panel : rowPanels) {
                int qty = panel.getSelectedQuantity();
                if (qty > 0) {
                    selectedEquipment.put(panel.getTypeId(), qty);
                }
            }
            confirmed = true;
            dispose();
        });

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }
    public Map<Integer, Integer> getSelectedEquipment() { return selectedEquipment; }

    private class EquipmentRowPanel extends JPanel {
        private final int typeId;
        private final JSpinner quantitySpinner;

        public EquipmentRowPanel(int typeId, String name, int available, int preselectedQty) {
            this.typeId = typeId;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
            setMaximumSize(new Dimension(600, 60));

            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JLabel lblAvailable;
            if (available > 0) {
                lblAvailable = new JLabel("Available: " + available);
                lblAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblAvailable.setForeground(new Color(0, 100, 0)); 
            } else {
                lblAvailable = new JLabel("Out of Stock");
                lblAvailable.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblAvailable.setForeground(Color.RED);
                lblName.setForeground(Color.GRAY);
            }
            
            infoPanel.add(lblName);
            infoPanel.add(lblAvailable);

            int safePreselected = Math.min(preselectedQty, available);
            SpinnerModel model = new SpinnerNumberModel(safePreselected, 0, available, 1);
            quantitySpinner = new JSpinner(model);
            quantitySpinner.setPreferredSize(new Dimension(60, 30));
            
            if (available == 0) quantitySpinner.setEnabled(false);

            add(infoPanel, BorderLayout.CENTER);
            add(quantitySpinner, BorderLayout.EAST);
        }

        public int getTypeId() { return typeId; }
        public int getSelectedQuantity() { return (int) quantitySpinner.getValue(); }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

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
