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
import java.util.LinkedHashMap;
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

public class GroupPlanMembersUI extends JDialog {

    private final int planId;

    private final DefaultTableModel model;
    private final JTable table;

    private final Map<Integer, Trainee> selectedMap = new LinkedHashMap<>();

    public GroupPlanMembersUI(JFrame parent, int preselectedPlanId) {
        super(parent, "FitWell - Group Plan Members", true);

        this.planId = preselectedPlanId;

        setSize(900, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel title = UIBuilder.LabelBuilder.of("Group Plan Members (ID: " + planId + ")")
                .font(new Font("Segoe UI", Font.BOLD, 26)).foreground(new Color(30, 55, 100))
                .build();

        JLabel subtitle = UIBuilder.LabelBuilder.of("Manage trainees in this Group Plan")
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

        // make columns readable
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(260);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 25, 0, 25),
                new LineBorder(new Color(230, 230, 230), 1)));


        JButton btnClose = new JButton("Close");
        JButton btnAdd = new JButton("Add Trainees");
        JButton btnRemove = new JButton("Remove Selected");
        JButton btnRefresh = new JButton("Refresh");

        styleButton(btnAdd, new Color(34, 139, 34)); 
        styleButton(btnRefresh, new Color(70, 130, 180)); 
        styleButton(btnRemove, new Color(178, 34, 34)); 
        styleButton(btnClose, new Color(100, 100, 100)); 

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 25, 10, 25));

        bottom.add(btnClose);
        bottom.add(btnRefresh);
        bottom.add(btnRemove);
        bottom.add(btnAdd);

        // add to main
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        // ==================
        // LOGIC
        // ==================
        btnClose.addActionListener(e -> dispose());

        btnRefresh.addActionListener(e -> refreshMembers());

        btnAdd.addActionListener(e -> onAdd(parent));

        btnRemove.addActionListener(e -> onRemove());

        refreshMembers();
    }

    private void onAdd(JFrame parent) {
        try {


            selectedMap.clear();

            // minimal: show all trainees
            int minAge = 0;
            int maxAge = 120;

            AddTraineesToNewGroupPlanForm dlg = new AddTraineesToNewGroupPlanForm(parent, planId, minAge, maxAge,
                    selectedMap);
            dlg.setVisible(true);

            int added = 0;
            for (Integer traineeId : selectedMap.keySet()) {
                boolean ok = PlanController.getInstance().addTraineeToGroupPlan(traineeId, planId);
                if (ok)
                    added++;
            }

            if (!selectedMap.isEmpty() && added == 0) {
                JOptionPane.showMessageDialog(this,
                        "Selected trainees are already in the group (or operation failed).");
            }

            refreshMembers();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRemove() {
        try{
            int[] viewRows = table.getSelectedRows();
            if (viewRows == null || viewRows.length == 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one trainee to remove.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Remove selected trainee(s) from group " + planId + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION)
                return;

            int removed = 0;
            for (int i = viewRows.length - 1; i >= 0; i--) {
                int modelRow = table.convertRowIndexToModel(viewRows[i]);
                int traineeId = Integer.parseInt(String.valueOf(model.getValueAt(modelRow, 0)));
                PlanController.getInstance().removeTraineeFromGroupPlan(traineeId, planId);
                removed++;
            }

            if (removed > 0) {
                JOptionPane.showMessageDialog(this, "Removed " + removed + " trainee(s).");
            }

            refreshMembers();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMembers() {
        model.setRowCount(0);

        List<Trainee> members = PlanController.getInstance().getGroupMembers(planId);
        LocalDateTime now = LocalDateTime.now();
        for (Trainee t : members) {
            int age = now.getYear() - t.getBirthDate().getYear(); // ignoring months differences
            model.addRow(new Object[] {
                    t.getTraineeId(), t.getFirstName(), t.getLastName(), age, t.getPhoneNumber(), t.getEmail()
            });
        }
    }

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
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(new Color(245, 245, 245));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(new Color(80, 80, 80));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
                l.setPreferredSize(new Dimension(l.getPreferredSize().width, 40));
                return l;
            }
        });
    }

    private void styleButton(JButton btn, Color bg) {
        UIBuilder.ButtonBuilder.of(btn)
            .font(new Font("Segoe UI", Font.BOLD, 14))     
            .background(bg)
            .foreground(Color.WHITE)
            .focus(false)
            .border(BorderFactory.createEmptyBorder(10, 20, 10, 20))
            .cursor(new Cursor(Cursor.HAND_CURSOR));
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