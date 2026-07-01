package com.fitwell.boundary.consultant;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.PlanController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class CreatePersonalPlanUI extends JDialog {

    private JComboBox<String> cmbTrainee;
    private JTextField txtStartDate;
    private JSpinner spDuration;
    private JTextArea txtGoals;
    private JTextArea txtDietary;
    private JComboBox<String> cmbDietitian;

    private PlanController controller = PlanController.getInstance();

    public CreatePersonalPlanUI(JFrame parent) {
        super(parent, "FitWell - Create Personal Plan", true);

        setSize(650, 800);
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
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel lblTitle = UIBuilder.LabelBuilder.of("Create Personal Plan")
            .font(new Font("Segoe UI", Font.BOLD, 24))
            .foreground(new Color(30, 55, 100)).build();
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridwidth = 1;
        mainPanel.add(lblTitle, gbc);

        // Trainee
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Trainee:"), gbc);
        gbc.gridy++;
        cmbTrainee = new JComboBox<>();
        styleComboBox(cmbTrainee);
        mainPanel.add(cmbTrainee, gbc);

        // Start date + duration (row)
        gbc.gridy++;
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        rowPanel.setOpaque(false);

        JPanel pnlStart = new JPanel(new BorderLayout());
        pnlStart.setOpaque(false);
        pnlStart.add(createStyledLabel("Start Date (yyyy-MM-dd):"), BorderLayout.NORTH);
        txtStartDate = createStyledTextField();
        txtStartDate.setText(LocalDate.now().toString());
        pnlStart.add(txtStartDate, BorderLayout.CENTER);

        JPanel pnlDur = new JPanel(new BorderLayout());
        pnlDur.setOpaque(false);
        pnlDur.add(createStyledLabel("Duration (weeks):"), BorderLayout.NORTH);
        spDuration = new JSpinner(new SpinnerNumberModel(12, 1, 104, 1));
        styleSpinner(spDuration);
        pnlDur.add(spDuration, BorderLayout.CENTER);

        rowPanel.add(pnlStart);
        rowPanel.add(pnlDur);
        mainPanel.add(rowPanel, gbc);

        // Goals
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Goals:"), gbc);

        gbc.gridy++;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.35;

        txtGoals = new JTextArea(4, 30);
        styleTextArea(txtGoals);

        JScrollPane goalsScroll = new JScrollPane(txtGoals);
        goalsScroll.setPreferredSize(new Dimension(0, 100));
        mainPanel.add(goalsScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        // Dietary
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Dietary Restrictions:"), gbc);

        gbc.gridy++;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.35;

        txtDietary = new JTextArea(4, 30);
        styleTextArea(txtDietary);

        JScrollPane dietaryScroll = new JScrollPane(txtDietary);
        dietaryScroll.setPreferredSize(new Dimension(0, 100));
        mainPanel.add(dietaryScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        // Dietitian
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Dietitian:"), gbc);
        gbc.gridy++;
        cmbDietitian = new JComboBox<>();
        styleComboBox(cmbDietitian);
        mainPanel.add(cmbDietitian, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        JButton btnCreate = new JButton("Create Plan");
        stylePrimaryButton(btnCreate);
        mainPanel.add(btnCreate, gbc);

        add(mainPanel);

        loadTrainees();
        loadDietitians();

        btnCreate.addActionListener(e -> onCreate());
    }

    private void loadTrainees() {
        List<String> trainees = PlanController.getInstance().getAllTraineesForDropdown();
        trainees.forEach(cmbTrainee::addItem);
    }

    private void loadDietitians() {
        cmbDietitian.addItem("0 - (None)");
        List<String> dietitians = PlanController.getInstance().getAllDietitiansForDropdown();
        dietitians.forEach(cmbDietitian::addItem);
    }

    private void onCreate() {
        try {
            if (cmbTrainee.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a trainee.");
                return;
            }

            int traineeId = parseLeadingInt(cmbTrainee.getSelectedItem().toString());
            int dietitianId = parseLeadingInt(cmbDietitian.getSelectedItem().toString());

            LocalDate ld = LocalDate.parse(txtStartDate.getText().trim());
            Date startDate = Date.valueOf(ld);

            int durationWeeks = (int) spDuration.getValue();
            String goals = txtGoals.getText().trim();
            String dietary = txtDietary.getText().trim();

            int planId = controller.createPersonalPlan(traineeId, startDate, durationWeeks, goals, dietary, dietitianId);

            JOptionPane.showMessageDialog(this, "Personal Plan created! Plan ID: " + planId);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parseLeadingInt(String s) {
        return Integer.parseInt(s.split("-")[0].trim());
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = UIBuilder.LabelBuilder.of(text)
            .font(new Font("Segoe UI", Font.BOLD, 12))
            .foreground(new Color(80, 80, 80)).build();
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        return txt;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        ((JComponent) box.getRenderer()).setBorder(new EmptyBorder(5, 5, 5, 5));
        box.setBorder(new LineBorder(new Color(200, 200, 200)));
    }

    private void styleSpinner(JSpinner spinner) {
        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editorComponent).getTextField()
                    .setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
        spinner.setBorder(new LineBorder(new Color(200, 200, 200)));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void stylePrimaryButton(JButton btn) {
        UIBuilder.ButtonBuilder.of(btn)
            .font(new Font("Segoe UI", Font.BOLD, 16))
            .background(new Color(34, 139, 34)).foreground(Color.WHITE)
            .focus(false).border(BorderFactory.createEmptyBorder(12, 0, 12, 0))
            .cursor(new Cursor(Cursor.HAND_CURSOR)).build();
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