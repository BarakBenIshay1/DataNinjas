package com.fitwell.boundary.consultant;

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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.PlanController;
import com.fitwell.entity.Trainee;

public class AddTraineesToNewGroupPlanForm extends JDialog {

    private final int planId;
    private final int minAge;
    private final int maxAge;
    private final Map<Integer, Trainee> selectedMap;

    private final DefaultTableModel model;
    private final JTable table;
    private Map<Integer, Trainee> traineeMap = new HashMap<>();

    private final PlanController planController = PlanController.getInstance();

    public AddTraineesToNewGroupPlanForm(JFrame parent,
            int minAge,
            int maxAge,
            Map<Integer, Trainee> selectedMap) {
        this(parent, -1, minAge, maxAge, selectedMap);
    }

    public AddTraineesToNewGroupPlanForm(JFrame parent,
            int planId,
            int minAge,
            int maxAge,
            Map<Integer, Trainee> selectedMap) {
        super(parent, "Add Trainees", true);

        this.planId = planId;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.selectedMap = selectedMap;

        setSize(900, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Background gradient
        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel title = UIBuilder.LabelBuilder.of("Add Trainees to Group Plan")
                .font(new Font("Segoe UI", Font.BOLD, 26)).foreground(new Color(30, 55, 100))
                .build();

        JLabel subtitle = UIBuilder.LabelBuilder
                .of("Select trainees from the list (Eligible age: " + minAge + " - " + maxAge + ")")
                .font(new Font("Segoe UI", Font.PLAIN, 14)).foreground(new Color(100, 100, 100))
                .build();

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(title);
        textWrapper.add(subtitle);

        headerPanel.add(textWrapper, BorderLayout.WEST);

        String[] columns = { "ID", "First Name", "Last Name", "Age", "Phone", "Email" };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 25, 0, 25),
                new LineBorder(new Color(230, 230, 230), 1)));


        JButton btnCancel = new JButton("Cancel");
        JButton btnAdd = new JButton("ADD");
        JButton btnRefresh = new JButton("Refresh");

        styleButton(btnAdd, new Color(34, 139, 34));
        styleButton(btnRefresh, new Color(70, 130, 180));
        styleButton(btnCancel, new Color(100, 100, 100));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 25, 10, 25));

        bottom.add(btnCancel);
        bottom.add(btnRefresh);
        bottom.add(btnAdd);

        // add to main
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);


        btnCancel.addActionListener(e -> dispose());

        btnRefresh.addActionListener(e -> loadTrainees());

        btnAdd.addActionListener(e -> {
            if (table.getSelectedRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one trainee.");
                return;
            }
            addSelectedRows();
            loadTrainees(); 
        });

        loadTrainees();
    }

    private void loadTrainees() {
        model.setRowCount(0);

        List<Trainee> trainees;

        if (planId != -1) {
            trainees = planController.getEligibleTraineesForPlan(planId, minAge, maxAge);
        } else {
            trainees = planController.getEligibleTrainees(minAge, maxAge);
        }


        LocalDateTime now = LocalDateTime.now();
        for (Trainee t : trainees) {
            traineeMap.put(t.getTraineeId(), t);
            int age = now.getYear() - t.getBirthDate().getYear(); // ignoring months differences
            if (!selectedMap.containsKey(t.getTraineeId())) {
                model.addRow(new Object[] {
                        t.getTraineeId(), t.getFirstName(), t.getLastName(), age, t.getPhoneNumber(), t.getEmail()
                });
            }
        }
    }
    
    private void addSelectedRows() {
        int[] viewRows = table.getSelectedRows();

        int added = 0;

        for (int vr : viewRows) {
            int mr = table.convertRowIndexToModel(vr);
            int id = Integer.parseInt(String.valueOf(model.getValueAt(mr, 0)));
            Trainee t = traineeMap.get(id)    ;
            if (!selectedMap.containsKey(id)) {
                selectedMap.put(id, t);
                added++;
            }
        }

        JOptionPane.showMessageDialog(this, "Added " + added + " trainees.");
    }

    // =========================
    // Styling helpers
    // =========================

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl = new UIBuilder.LabelBuilder(lbl)
                        .background(new Color(245, 245, 245)).foreground(new Color(80, 80, 80))
                        .font(new Font("Segoe UI", Font.BOLD, 13))
                        .border(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)))
                        .preferredSize(new Dimension(lbl.getPreferredSize().width, 40))
                        .build();
                // lbl.setBackground(new Color(245, 245, 245));
                // lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                // lbl.setForeground(new Color(80, 80, 80));
                // lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200,
                // 200)));
                // lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 40));
                return lbl;
            }
        });
    }

    private void styleButton(JButton btn, Color bg) {
        btn = UIBuilder.ButtonBuilder.of(btn)
                .font(new Font("Segoe UI", Font.BOLD, 14)).background(bg).foreground(Color.WHITE)
                .focus(false).border(BorderFactory.createEmptyBorder(10, 20, 10, 20))
                .cursor(new Cursor(Cursor.HAND_CURSOR)).build();

        // btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // btn.setBackground(bg);
        // btn.setForeground(Color.WHITE);
        // btn.setFocusPainted(false);
        // btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        // btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static class AppBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, new Color(248, 250, 255),
                    0, getHeight(), new Color(225, 235, 245));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}