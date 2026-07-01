package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.PlanController;
import com.fitwell.entity.Trainee;

public class CreateGroupPlanUI extends JDialog {

    private JSpinner spMinAge;
    private JSpinner spMaxAge;
    
    private JSpinner spDuration; 
    private JTextField txtPreferredTypes;
    private JTextArea txtGuidelines;

    private final Map<Integer, Trainee> selected = new LinkedHashMap<>();

    private PlanController controller = PlanController.getInstance();

    public CreateGroupPlanUI(JFrame parent) {
        super(parent, "FitWell - Create Group Plan", true);

        setSize(650, 850);
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

        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.gridy = 0;
        root.weightx = 1.0;
        root.weighty = 1.0;
        root.fill = GridBagConstraints.NONE;        
        root.anchor = GridBagConstraints.CENTER;
        root.insets = new Insets(20, 20, 20, 20);  
        add(mainPanel, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Create Group Plan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(30, 55, 100));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle, gbc);

        // Age row
        gbc.gridy++;
        JPanel ageRow = new JPanel(new GridLayout(1, 2, 15, 0));
        ageRow.setOpaque(false);

        JPanel pnlMin = new JPanel(new BorderLayout());
        pnlMin.setOpaque(false);
        pnlMin.add(createStyledLabel("Min Age:"), BorderLayout.NORTH);
        spMinAge = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));
        styleSpinner(spMinAge);
        pnlMin.add(spMinAge, BorderLayout.CENTER);

        JPanel pnlMax = new JPanel(new BorderLayout());
        pnlMax.setOpaque(false);
        pnlMax.add(createStyledLabel("Max Age:"), BorderLayout.NORTH);
        spMaxAge = new JSpinner(new SpinnerNumberModel(65, 0, 120, 1));
        styleSpinner(spMaxAge);
        pnlMax.add(spMaxAge, BorderLayout.CENTER);

        ageRow.add(pnlMin);
        ageRow.add(pnlMax);
        mainPanel.add(ageRow, gbc);

        // ✅ Duration Row - הוספנו ל-UI
        gbc.gridy++;
        JPanel pnlDuration = new JPanel(new BorderLayout());
        pnlDuration.setOpaque(false);
        pnlDuration.add(createStyledLabel("Duration (Weeks):"), BorderLayout.NORTH);
        spDuration = new JSpinner(new SpinnerNumberModel(4, 1, 52, 1)); 
        styleSpinner(spDuration);
        pnlDuration.add(spDuration, BorderLayout.CENTER);
        mainPanel.add(pnlDuration, gbc);

        // Preferred types
        gbc.gridy++;
        mainPanel.add(createStyledLabel("Preferred Class Types (comma separated):"), gbc);
        gbc.gridy++;
        txtPreferredTypes = createStyledTextField();
        txtPreferredTypes.setText("Cardio, Strength");
        mainPanel.add(txtPreferredTypes, gbc);

        // Guidelines
        gbc.gridy++;
        mainPanel.add(createStyledLabel("General Guidelines:"), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.55;

        txtGuidelines = new JTextArea(6, 30);
        styleTextArea(txtGuidelines);
        JScrollPane guidelinesScroll = new JScrollPane(txtGuidelines);
        guidelinesScroll.setPreferredSize(new Dimension(0, 180));
        mainPanel.add(guidelinesScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;

        // Add trainees button
        gbc.gridy++;
        gbc.insets = new Insets(14, 0, 8, 0);

        JButton btnAddTrainees = createSecondaryButton("👥 Add Trainees");
        btnAddTrainees.setPreferredSize(new Dimension(520, 46));
        mainPanel.add(btnAddTrainees, gbc);

        // Create button
        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);

        JButton btnCreate = new JButton("Create Group Plan");
        stylePrimaryButton(btnCreate);
        btnCreate.setPreferredSize(new Dimension(520, 60));
        mainPanel.add(btnCreate, gbc);

        btnAddTrainees.addActionListener(e -> onOpenTraineesDialog(parent));
        btnCreate.addActionListener(e -> onCreate());
    }

    private void onOpenTraineesDialog(JFrame parent) {
        int minAge = (int) spMinAge.getValue();
        int maxAge = (int) spMaxAge.getValue();
        if (minAge > maxAge) {
            JOptionPane.showMessageDialog(this, "Min age cannot be greater than max age.");
            return;
        }

        AddTraineesToNewGroupPlanForm dlg =
                new AddTraineesToNewGroupPlanForm(parent, minAge, maxAge, selected);
        dlg.setVisible(true);
    }

    private void onCreate() {
        try {
            int minAge = (int) spMinAge.getValue();
            int maxAge = (int) spMaxAge.getValue();
            int durationInWeeks = (int) spDuration.getValue(); 

            if (minAge > maxAge) {
                JOptionPane.showMessageDialog(this, "Min age cannot be greater than max age.");
                return;
            }

            String preferred = txtPreferredTypes.getText().trim();
            String guidelines = txtGuidelines.getText().trim();

            int planId = controller.createGroupPlan(minAge, maxAge, preferred, guidelines, durationInWeeks);

            int added = 0;
            for (Integer traineeId : selected.keySet()) {
                boolean ok = PlanController.getInstance().addTraineeToGroupPlan(traineeId, planId);
                if (ok) added++;
            }

            String strAdded = (added > 0 ? ("\nTrainees added: " + added) : "");
            JOptionPane.showMessageDialog(
                    this,
                    "Group Plan created! Plan ID: " + planId +strAdded,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== STYLE HELPERS =====

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
                new EmptyBorder(8, 8, 8, 8)
        ));
        return txt;
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

    private JButton createSecondaryButton(String text) {
        JButton btn = UIBuilder.ButtonBuilder.of(text).font(new Font("Segoe UI", Font.PLAIN, 14))
        .background(new Color(230, 240, 255)).foreground(new Color(30, 55, 100))
        .focus(false).border(BorderFactory.createLineBorder(new Color(180, 200, 230)))
        .cursor(new Cursor(Cursor.HAND_CURSOR)).build();
        return btn;
    }

    private void stylePrimaryButton(JButton btn) {
        UIBuilder.ButtonBuilder.of(btn)
        .font(new Font("Segoe UI", Font.BOLD, 16)).focus(false)
        .background(new Color(34, 139, 34)).foreground(Color.WHITE)
        .border(BorderFactory.createEmptyBorder(12, 0, 12, 0))
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