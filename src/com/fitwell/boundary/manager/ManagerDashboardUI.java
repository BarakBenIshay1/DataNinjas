package com.fitwell.boundary.manager;

import java.awt.BorderLayout;
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
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.EditTraineeListForm;
import com.fitwell.boundary.UIBuilder;
import com.fitwell.boundary.login.RoleSelectionUI;
import com.fitwell.control.ManagerController;
import com.fitwell.control.ManagerController.PairData;

public class ManagerDashboardUI extends JFrame {

    private JTextField txtId, txtFirst, txtLast, txtEmail, txtPhone;
    private JTextField txtDietary, txtGoals; 
    private JSpinner dateSpinner;
    private JComboBox<PairData> cmbUpdateMethod;
    private JComboBox<PairData> cmbPlans;
    private JComboBox<String> cmbDietitians; 

    private ManagerController controller = ManagerController.getInstance();

    public ManagerDashboardUI() {
        super("FitWell - Manager Dashboard");
        setSize(750, 650); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // פאנל רקע ראשי
        JPanel bgPanel = new AppBackgroundPanel();
        bgPanel.setLayout(new BorderLayout(10, 15));
        bgPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(bgPanel);

        // =========================================
        // פאנל עליון: כותרת + כפתור התנתקות (חזרה)
        // =========================================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("New Trainee Registration");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 55, 100));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnBack = new JButton("Logout / Switch Role");
        btnBack.setBackground(new Color(220, 53, 69)); // אדום עדין להתנתקות
        btnBack.setForeground(Color.WHITE);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new RoleSelectionUI().setVisible(true); // חזרה למסך התפקידים
            dispose();
        });
        topPanel.add(btnBack, BorderLayout.EAST);

        bgPanel.add(topPanel, BorderLayout.NORTH);

        // =========================================
        // פאנל אמצע: כפתור עריכה + טופס שדות ב-2 עמודות
        // =========================================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // כפתור מעבר למסך ניהול/עריכה
        JButton btnManageExisting = new JButton("Manage / Edit Existing Trainees");
        btnManageExisting.setBackground(new Color(0, 102, 204));
        btnManageExisting.setForeground(Color.WHITE);
        btnManageExisting.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnManageExisting.setFocusPainted(false);
        btnManageExisting.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnManageExisting.setPreferredSize(new Dimension(0, 40));
        btnManageExisting.addActionListener(e -> {
            new EditTraineeListForm(this).setVisible(true);
        });
        centerPanel.add(btnManageExisting, BorderLayout.NORTH);

        // פאנל הטופס עצמו - שימוש ב-GridBagLayout כדי לסדר 2 עמודות!
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // מרווחים בין השדות
        gbc.weightx = 0.5;

        // --- שורה 1: ת"ז + תאריך לידה ---
        gbc.gridy = 0;
        gbc.gridx = 0; formPanel.add(createLabel("Trainee ID:"), gbc);
        gbc.gridx = 1; txtId = new JTextField(); formPanel.add(txtId, gbc);
        gbc.gridx = 2; formPanel.add(createLabel("Birth Date:"), gbc);
        gbc.gridx = 3; 
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);

        // --- שורה 2: שם פרטי + שם משפחה ---
        gbc.gridy = 1;
        gbc.gridx = 0; formPanel.add(createLabel("First Name:"), gbc);
        gbc.gridx = 1; txtFirst = new JTextField(); formPanel.add(txtFirst, gbc);
        gbc.gridx = 2; formPanel.add(createLabel("Last Name:"), gbc);
        gbc.gridx = 3; txtLast = new JTextField(); formPanel.add(txtLast, gbc);

        // --- שורה 3: אימייל + טלפון ---
        gbc.gridy = 2;
        gbc.gridx = 0; formPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; txtEmail = new JTextField(); formPanel.add(txtEmail, gbc);
        gbc.gridx = 2; formPanel.add(createLabel("Phone Number:"), gbc);
        gbc.gridx = 3; txtPhone = new JTextField(); formPanel.add(txtPhone, gbc);

        // --- שורה 4: שיטת עדכון + בחירת תוכנית ---
        gbc.gridy = 3;
        gbc.gridx = 0; formPanel.add(createLabel("Update Method:"), gbc);
        gbc.gridx = 1; 
        cmbUpdateMethod = new JComboBox<PairData>();
        for(PairData method : controller.getUpdateMethods()){
            cmbUpdateMethod.addItem(method);
        }
        formPanel.add(cmbUpdateMethod, gbc);
        gbc.gridx = 2; formPanel.add(createLabel("Select Plan:"), gbc);
        gbc.gridx = 3; 
        cmbPlans = new JComboBox<PairData>();
        List<PairData> plans = controller.getAllPlans();
        for (PairData p : plans) cmbPlans.addItem(p);
        formPanel.add(cmbPlans, gbc);

        // --- שורה 5: הגבלות תזונה + מטרות (אישי בלבד) ---
        gbc.gridy = 4;
        gbc.gridx = 0; formPanel.add(createLabel("Dietary (Personal Only):"), gbc);
        gbc.gridx = 1; txtDietary = new JTextField(); formPanel.add(txtDietary, gbc);
        gbc.gridx = 2; formPanel.add(createLabel("Goals (Personal Only):"), gbc);
        gbc.gridx = 3; txtGoals = new JTextField(); formPanel.add(txtGoals, gbc);

        // --- שורה 6: דיאטנית (אישי בלבד) ---
        gbc.gridy = 5;
        gbc.gridx = 0; formPanel.add(createLabel("Dietitian (Personal Only):"), gbc);
        gbc.gridx = 1; 
        cmbDietitians = new JComboBox<>();
        List<String> diets = ManagerController.getInstance().getAllDietitians();
        for (String d : diets) cmbDietitians.addItem(d);
        formPanel.add(cmbDietitians, gbc);
        
        centerPanel.add(formPanel, BorderLayout.CENTER);
        bgPanel.add(centerPanel, BorderLayout.CENTER);

        // =========================================
        // פאנל תחתון: כפתור הרשמה ירוק
        // =========================================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        
        JButton btnRegister = new JButton("Register Trainee");
        btnRegister.setBackground(new Color(34, 139, 34));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setPreferredSize(new Dimension(300, 45));
        bottomPanel.add(btnRegister);

        bgPanel.add(bottomPanel, BorderLayout.SOUTH);

        // פעולות המאזינים
        cmbPlans.addActionListener(e -> togglePersonalFields());
        
        btnRegister.addActionListener(e -> {
            try {
                String idText = txtId.getText().trim();
                if (!idText.matches("\\d{4,9}")) {
                    JOptionPane.showMessageDialog(this, "ID must contain 4-9 digits only.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(idText);

                if (ManagerController.getInstance().isTraineeExists(id)) {
                    JOptionPane.showMessageDialog(this, "A trainee with this ID already exists in the system!", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String fName = txtFirst.getText().trim();
                String lName = txtLast.getText().trim();
                if (fName.isEmpty() || lName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "First and Last names are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String email = txtEmail.getText().trim();
                if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String phone = txtPhone.getText().trim();
                if (!phone.matches("05\\d{8}|0\\d{8}")) {
                    JOptionPane.showMessageDialog(this, "Invalid phone number format (e.g. 0501234567).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date utilDate = (Date) dateSpinner.getValue();
                if (utilDate.after(new Date())) {
                    JOptionPane.showMessageDialog(this, "Birth date cannot be in the future.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                java.sql.Date birthDate = new java.sql.Date(utilDate.getTime());

                PairData updateMethod = (PairData) cmbUpdateMethod.getSelectedItem();
                if (updateMethod == null) {
                    JOptionPane.showMessageDialog(this, "Please select an update method", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }                
                PairData plan = (PairData) cmbPlans.getSelectedItem();
              if (plan == null ) {
                    JOptionPane.showMessageDialog(this, "Please select an update method", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }                  
                
                String dietary = txtDietary.getText().trim();
                String goals = txtGoals.getText().trim();
                int dietitianId = 0;
                
                if (cmbDietitians.isEnabled() && cmbDietitians.getSelectedIndex() >= 0) {
                    String selectedDietitian = (String) cmbDietitians.getSelectedItem();
                    dietitianId = Integer.parseInt(selectedDietitian.split(" - ")[0]);
                }

                boolean success = ManagerController.getInstance().registerTrainee(
                        id, fName, lName, email, phone, birthDate, updateMethod.getId(), 
                        plan.getId(), dietary, goals, dietitianId);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Trainee registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please verify all fields are filled correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        togglePersonalFields();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = UIBuilder.LabelBuilder.of(text)
            .font(new Font("Segoe UI", Font.BOLD, 12)).foreground(new Color(60, 60, 60))
            .build();
        return lbl;
    }

    private void togglePersonalFields() {
        PairData selected = (PairData) cmbPlans.getSelectedItem();
        boolean isPersonal = selected != null && selected.getId() == ManagerController.PERSONAL_PLAN;
        txtDietary.setEnabled(isPersonal);
        txtGoals.setEnabled(isPersonal);
        cmbDietitians.setEnabled(isPersonal);
    }

    private void clearFields() {
        txtId.setText("");
        txtFirst.setText("");
        txtLast.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtDietary.setText("");
        txtGoals.setText("");
        if (cmbPlans.getItemCount() > 0) cmbPlans.setSelectedIndex(0);
        if (cmbUpdateMethod.getItemCount() > 0) cmbUpdateMethod.setSelectedIndex(0);
        dateSpinner.setValue(new Date());
        togglePersonalFields();
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