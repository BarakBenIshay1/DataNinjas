package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.control.TrainingClassController;
import com.fitwell.control.TrainingClassController.ClassType;

public class EditTrainingClassForm extends JDialog {

    private JTextField txtName;
    private JComboBox<ClassType> cmbClassType;
    private JTextField txtMax;
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    
    private final int classId;
    private Map<Integer, Integer> currentEquipment; 

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private TrainingClassController controller = TrainingClassController.getInstance();

    public EditTrainingClassForm(Dialog owner, Object[] classRow) {
        super(owner, "Edit Training Class", true);
        this.classId = Integer.parseInt(classRow[0].toString());
        
        this.currentEquipment = controller.getClassEquipment(classId);

        setSize(500, 600); 
        setLocationRelativeTo(owner);
        
        JPanel background = new AppBackgroundPanel();
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Edit Schedule & Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 55, 100));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);

        // --- Class Name ---
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Class Name:"), gbc);
        gbc.gridy++;
        txtName = createStyledTextField();
        txtName.setText(classRow[1].toString());
        mainPanel.add(txtName, gbc);

        // --- Dates ---
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Schedule:"), gbc);
        gbc.gridy++;
        
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        datePanel.setOpaque(false);
        
        Date initialStart = parseDateFromTable(classRow[2].toString());
        Date initialEnd = parseDateFromTable(classRow[3].toString());

        startSpinner = createDateTimeSpinner(initialStart);
        endSpinner = createDateTimeSpinner(initialEnd);
        
        JPanel pnlStart = new JPanel(new BorderLayout());
        pnlStart.setOpaque(false);
        pnlStart.add(new JLabel("Start:"), BorderLayout.NORTH);
        pnlStart.add(startSpinner, BorderLayout.CENTER);

        JPanel pnlEnd = new JPanel(new BorderLayout());
        pnlEnd.setOpaque(false);
        pnlEnd.add(new JLabel("End:"), BorderLayout.NORTH);
        pnlEnd.add(endSpinner, BorderLayout.CENTER);

        datePanel.add(pnlStart);
        datePanel.add(pnlEnd);
        mainPanel.add(datePanel, gbc);

        // --- Type & Max Participants ---
        gbc.gridy++;
        JPanel detailsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        detailsPanel.setOpaque(false);

        JPanel pnlType = new JPanel(new BorderLayout());
        pnlType.setOpaque(false);
        pnlType.add(createStyledLabel("Type:"), BorderLayout.NORTH);
        cmbClassType = new JComboBox<ClassType>();
        controller.getClassTypeList().forEach(cmbClassType::addItem);
        cmbClassType.setSelectedItem(controller.getClassTypeById((int)classRow[4]));

        pnlType.add(cmbClassType, BorderLayout.CENTER);

        JPanel pnlMax = new JPanel(new BorderLayout());
        pnlMax.setOpaque(false);
        pnlMax.add(createStyledLabel("Max Participants:"), BorderLayout.NORTH);
        txtMax = createStyledTextField();
        txtMax.setText(classRow[5].toString());
        pnlMax.add(txtMax, BorderLayout.CENTER);

        detailsPanel.add(pnlType);
        detailsPanel.add(pnlMax);
        mainPanel.add(detailsPanel, gbc);

        // --- Edit Equipment Button ---
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 5, 0);
        JButton btnEditEquipment = new JButton("🏋️ Edit Equipment");
        btnEditEquipment.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnEditEquipment.setBackground(new Color(70, 130, 180));
        btnEditEquipment.setForeground(Color.WHITE);
        btnEditEquipment.setFocusPainted(false);
        btnEditEquipment.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnEditEquipment.addActionListener(e -> {
            LocalDateTime tempStart = convertToLocalDateTime(startSpinner);
            LocalDateTime tempEnd = convertToLocalDateTime(endSpinner);
            
            EditEquipmentDialog dlg = new EditEquipmentDialog(this, tempStart, tempEnd, classId, currentEquipment);
            dlg.setVisible(true);
            
            if (dlg.isConfirmed()) {
                this.currentEquipment = dlg.getSelectedEquipment();
                JOptionPane.showMessageDialog(this, "Equipment selection updated temporarily.\nClick 'Save Changes' to apply.");
            }
        });
        mainPanel.add(btnEditEquipment, gbc);

        // --- Action Buttons ---
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);

        JButton btnCancelClass = new JButton("Cancel Class");
        styleDangerButton(btnCancelClass);
        JButton btnSave = new JButton("Save Changes");
        stylePrimaryButton(btnSave);

        btnPanel.add(btnCancelClass);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, gbc);

        add(mainPanel);

        btnCancelClass.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to CANCEL this class?\nIt will be marked as 'Cancelled'.",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.cancelTrainingClass(classId)) {
                    JOptionPane.showMessageDialog(this, "Class cancelled successfully.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel class.");
                }
            }
        });

        btnSave.addActionListener(e -> onSave());
    }

    private void onSave() {
        try {
            String newName = txtName.getText().trim();
            ClassType newType = (ClassType)cmbClassType.getSelectedItem();
            int newMax = Integer.parseInt(txtMax.getText().trim());
            LocalDateTime newStart = convertToLocalDateTime(startSpinner);
            LocalDateTime newEnd = convertToLocalDateTime(endSpinner);

            if (!newEnd.isAfter(newStart)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time");
                return;
            }

            boolean success = controller.updateClassDetails(
                    classId, newName, newStart, newEnd, newType.getClassTypeId(), newMax, currentEquipment);

            if (success) {
                JOptionPane.showMessageDialog(this, "Class details and equipment updated successfully!");
                dispose();
            }

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Cannot save:\n" + ex.getMessage(), "Conflict", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Helpers
    private Date parseDateFromTable(String dateStr) {
        try {
            if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
            LocalDateTime ldt = LocalDateTime.parse(dateStr, FORMAT);
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) { return new Date(); }
    }

    private JSpinner createDateTimeSpinner(Date initialValue) {
        SpinnerDateModel model = new SpinnerDateModel(initialValue, null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);
        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editorComponent).getTextField().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        spinner.setBorder(new LineBorder(new Color(200, 200, 200)));
        return spinner;
    }

    private LocalDateTime convertToLocalDateTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)), new EmptyBorder(6,6,6,6)));
        return txt;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(34, 139, 34));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDangerButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(220, 53, 69));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
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