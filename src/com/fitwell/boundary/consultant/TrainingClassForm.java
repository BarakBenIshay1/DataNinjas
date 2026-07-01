package com.fitwell.boundary.consultant;

import com.fitwell.boundary.EquipmentSelectionDialog;
import com.fitwell.boundary.TipSelectionDialog;
import com.fitwell.control.TrainingClassController;
import com.fitwell.control.TrainingClassController.ClassType;
import com.fitwell.control.TrainingClassController.PlanItem;
import com.fitwell.entity.Tip;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingClassForm extends JFrame {

    private JTextField nameField;
    private JTextField maxField;
    private JComboBox<ClassType> typeBox;
    private JSpinner startSpinner;
    private JSpinner endSpinner;

    private JComboBox<PlanItem> planBox;
    private JRadioButton rbGroup;
    private JRadioButton rbPersonal;
    private JButton tipsBtn;
    private JButton equipBtn;
    private JButton createBtn;

    private List<Tip> selectedTips;
    private Map<Integer, Integer> equipmentMap;

    private TrainingClassController controller = TrainingClassController.getInstance();

    public TrainingClassForm() {
        super("FitWell - Schedule New Class");
        setSize(550, 720); // גובה מותאם לתוכן
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // אתחול המפה
        equipmentMap = new HashMap<>();

        // רקע כללי
        setContentPane(new AppBackgroundPanel());
        setLayout(new GridBagLayout());

        // === הפאנל הלבן המעוצב ===
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 35, 25, 35)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // --- כותרת ---
        JLabel lblTitle = new JLabel("Schedule New Class");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(30, 55, 100));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);

        // --- Class Name ---
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Class Name:"), gbc);
        gbc.gridy++;
        nameField = createStyledTextField();
        mainPanel.add(nameField, gbc);

        // --- Dates (שורה אחת לשני תאריכים) ---
        gbc.gridy++;
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        datePanel.setOpaque(false);

        JPanel pnlStart = new JPanel(new BorderLayout());
        pnlStart.setOpaque(false);
        pnlStart.add(createStyledLabel("Start Time:"), BorderLayout.NORTH);
        startSpinner = createDateTimeSpinner();
        pnlStart.add(startSpinner, BorderLayout.CENTER);

        JPanel pnlEnd = new JPanel(new BorderLayout());
        pnlEnd.setOpaque(false);
        pnlEnd.add(createStyledLabel("End Time:"), BorderLayout.NORTH);
        endSpinner = createDateTimeSpinner();
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
        pnlType.add(createStyledLabel("Class Type:"), BorderLayout.NORTH);
        typeBox = new JComboBox<ClassType>();
        controller.getClassTypeList().forEach(typeBox::addItem);

        styleComboBox(typeBox);
        pnlType.add(typeBox, BorderLayout.CENTER);

        JPanel pnlMax = new JPanel(new BorderLayout());
        pnlMax.setOpaque(false);
        pnlMax.add(createStyledLabel("Max Participants:"), BorderLayout.NORTH);
        maxField = createStyledTextField();
        pnlMax.add(maxField, BorderLayout.CENTER);

        detailsPanel.add(pnlType);
        detailsPanel.add(pnlMax);
        mainPanel.add(detailsPanel, gbc);

        // --- Class Mode (Radio Buttons) ---
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Class Mode:"), gbc);
        gbc.gridy++;

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modePanel.setOpaque(false);

        rbGroup = new JRadioButton("Group Plan");
        rbPersonal = new JRadioButton("Personal Plan");
        styleRadioButton(rbGroup);
        styleRadioButton(rbPersonal);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbGroup);
        bg.add(rbPersonal);

        modePanel.add(rbGroup);
        modePanel.add(Box.createHorizontalStrut(20));
        modePanel.add(rbPersonal);
        mainPanel.add(modePanel, gbc);

        // --- Training Plan Selection ---
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Training Plan ID:"), gbc);
        gbc.gridy++;
        planBox = new JComboBox<>();
        styleComboBox(planBox);
        mainPanel.add(planBox, gbc);

        // --- Resources Buttons ---
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 20, 0);
        JPanel resourcesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        resourcesPanel.setOpaque(false);

        tipsBtn = createSecondaryButton("Select Tips");
        equipBtn = createSecondaryButton("Add Equipment");

        resourcesPanel.add(tipsBtn);
        resourcesPanel.add(equipBtn);
        mainPanel.add(resourcesPanel, gbc);

        // --- Create Button ---
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        createBtn = new JButton("Create Class");
        stylePrimaryButton(createBtn);
        mainPanel.add(createBtn, gbc);

        add(mainPanel);

        // ===== חיבור המאזינים (הלוגיקה המקורית שלך) =====
        setupListeners();
    }

    // FIXME delete
    // ==========================================
    // LOGIC METHODS (הקוד שלך)
    // ==========================================

    private void setupListeners() {
        rbGroup.addActionListener(e -> loadGroupPlans());
        rbPersonal.addActionListener(e -> loadPersonalPlans());

        tipsBtn.addActionListener(e -> openTipDialog());
        equipBtn.addActionListener(e -> openEquipmentDialog());
        createBtn.addActionListener(e -> createClass());
    }

    // TODO use Plan class class to String
    private void loadGroupPlans() {
        planBox.removeAllItems();
        List<PlanItem> plans = controller.getAllGroupPlanIds();
        if (plans != null)
            plans.forEach(planBox::addItem);
    }

    // TODO use Plan class class to String
    private void loadPersonalPlans() {
        planBox.removeAllItems();
        List<PlanItem> plans = controller.getAllPersonalPlanIds();
        if (plans != null)
            plans.forEach(planBox::addItem);
    }

    private void openTipDialog() {
        // שחזור הלוגיקה המקורית שלך:
        // 1. שליפת כל הטיפים מהקונטרולר
        List<Tip> allTips = TrainingClassController.getInstance().getAllTips();

        // 2. פתיחת הדיאלוג
        TipSelectionDialog dlg = new TipSelectionDialog(this, allTips);
        dlg.setVisible(true);

        // 3. שמירת הטיפים שנבחרו
        this.selectedTips = dlg.getSelectedTips();

        // פידבק ויזואלי בכפתור (כמו שעשינו לציוד)
        if (selectedTips != null && !selectedTips.isEmpty()) {
            tipsBtn.setText("Tips Selected (" + selectedTips.size() + ") ✅");
            tipsBtn.setBackground(new Color(220, 255, 220));
        }
    }

    private void openEquipmentDialog() {
        LocalDateTime start = convertToLocalDateTime(startSpinner);
        LocalDateTime end = convertToLocalDateTime(endSpinner);

        if (!end.isAfter(start)) {
            JOptionPane.showMessageDialog(this, "Please set valid Start and End times first.");
            return;
        }

        EquipmentSelectionDialog dlg = new EquipmentSelectionDialog(this, start, end);
        dlg.setVisible(true);

        Map<Integer, Integer> result = dlg.getSelectedEquipment();
        if (result != null && !result.isEmpty()) {
            this.equipmentMap = result;
            equipBtn.setText("Equipment Added ✅");
            equipBtn.setBackground(new Color(220, 255, 220));
        }
    }

    private void createClass() {
        try {
            String name = nameField.getText().trim();
            String maxStr = maxField.getText().trim();

            if (name.isEmpty())
                throw new IllegalArgumentException("Class name is required");
            if (maxStr.isEmpty())
                throw new IllegalArgumentException("Max participants required");

            int max = Integer.parseInt(maxStr);
            ClassType type = (ClassType) typeBox.getSelectedItem();

            LocalDateTime start = convertToLocalDateTime(startSpinner);
            LocalDateTime end = convertToLocalDateTime(endSpinner);

            PlanItem planItem = (PlanItem) planBox.getSelectedItem();
            if (planItem == null) {
                throw new IllegalArgumentException("You must select a training plan (Group/Personal)");
            }

            controller.createTrainingClass(name,start,end,type.getClassTypeId(),max,selectedTips,planItem.getPlanId(),equipmentMap);

            JOptionPane.showMessageDialog(this, "Training class created successfully!");
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Max participants must be a number", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // UI HELPER METHODS (עיצוב)
    // ==========================================

    private JSpinner createDateTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);

        // עיצוב ה-Spinner
        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editorComponent).getTextField().setBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        spinner.setBorder(new LineBorder(new Color(200, 200, 200)));
        return spinner;
    }

    private LocalDateTime convertToLocalDateTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withSecond(0)
                .withNano(0);
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
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 8, 8, 8)));
        return txt;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        ((JComponent) box.getRenderer()).setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private void styleRadioButton(JRadioButton rb) {
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rb.setBackground(Color.WHITE);
        rb.setFocusPainted(false);
        rb.setOpaque(false);
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(new Color(230, 240, 255));
        btn.setForeground(new Color(30, 55, 100));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230)));
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(34, 139, 34)); // ירוק
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(40, 160, 40));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(34, 139, 34));
            }
        });
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