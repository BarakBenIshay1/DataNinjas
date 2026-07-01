package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import com.fitwell.boundary.UIBuilder;
import com.fitwell.control.EquipmentImportController;
import com.fitwell.control.EquipmentManagementController;
import com.fitwell.control.EquipmentManagementController.EquipmentItemView;
import com.fitwell.control.TrainingClassController; 

public class ManageStockUI extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter; 
    private EquipmentManagementController controller;

    private static final Path MONTHLY_REPORT_PATH = Path.of("data", "equipment_updates.json");

    public ManageStockUI(JFrame owner) {
        super(owner, "Inventory Management", true);
        setSize(1000, 650); 
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        controller = EquipmentManagementController.getInstance();
        
        JPanel mainPanel = UIBuilder.PanelBuilder.of(new BorderLayout()).background(new Color(245, 247, 250)).build();
        setContentPane(mainPanel);

        JPanel topPanel = UIBuilder.PanelBuilder.of(new BorderLayout()).opaque(false)
        		.border(new EmptyBorder(10, 20, 10, 20)).build();

        JPanel searchPanel = UIBuilder.PanelBuilder.of(new FlowLayout(FlowLayout.LEFT)).opaque(false)
        		.build();

        JLabel lblSearch = new JLabel("🔍 Search Name: ");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);

        JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        importPanel.setOpaque(false);
        JButton btnImport = createOutlineButton("⬇ Import Monthly Report");
        btnImport.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13)); 
        btnImport.addActionListener(e -> importReport());
        importPanel.add(btnImport);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(importPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // CENTRAL TABLE
        String[] columns = {"Serial", "Item Name", "Type ID", "Location", "Review Needed", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);
        
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            
            private void filterTable() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null); 
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });
        // -------------------------------------------

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(0, 20, 10, 20));
        tableWrapper.setOpaque(false);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableWrapper, BorderLayout.CENTER);

        // BOTTOM ACTIONS =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        bottomPanel.setOpaque(false);

        JButton btnReviewed = createActionButton("Mark as Reviewed", new Color(70, 130, 180));
        JButton btnFunc = createActionButton("Mark Functional", new Color(46, 139, 87)); 
        JButton btnNonFunc = createActionButton("Mark Non-Functional", new Color(200, 60, 60)); 
        JButton btnRemove = createActionButton("Remove Item", new Color(100, 100, 100)); 
        
        bottomPanel.add(btnReviewed);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnFunc);
        bottomPanel.add(btnNonFunc);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(btnRemove);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        btnReviewed.addActionListener(e -> markAsReviewed());
        btnFunc.addActionListener(e -> updateStatus(true));
        btnNonFunc.addActionListener(e -> updateStatus(false));
        btnRemove.addActionListener(e -> removeItem());

        refreshTable();
    }


    private void refreshTable() {
        model.setRowCount(0);
        List<EquipmentItemView> items = controller.getAllEquipmentTableData();

        for (EquipmentItemView item : items) {
            String serial = item.SerialNumber;
            String name = item.name;
            int typeId = item.equipomentTypeID;
            int shelf = item.shelfNumber;
            int x = item.LocationX;
            int y = item.LocationY;
            boolean needsReview = item.needsReview;
            boolean isFunctional = item.isFunctional;

            String location = String.format("S:%d [x:%d, y:%d]", shelf, x, y);
            String status = isFunctional ? "Functional" : "Non-Functional";
            String review = needsReview ? "⚠️ YES" : "No";

            model.addRow(new Object[]{serial, name, typeId, location, review, status});
        }
    }

    private void markAsReviewed() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item marked with ⚠️.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String serial = (String) model.getValueAt(modelRow, 0);
        String currentReviewStatus = (String) model.getValueAt(modelRow, 4);

        if (!currentReviewStatus.contains("YES")) {
            JOptionPane.showMessageDialog(this, "This item does not need review.");
            return;
        }

        boolean success = controller.markItemAsReviewed(serial);
        
        if (success) {
            model.setValueAt("No", modelRow, 4); 
            JOptionPane.showMessageDialog(this, "Item marked as reviewed! ✅");
        } else {
            JOptionPane.showMessageDialog(this, "Database Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(boolean isFunctional) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item.");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String serial = (String) model.getValueAt(modelRow, 0);

        if (!isFunctional) {
            List<EquipmentManagementController.ShortageInfo> affected = controller.getAffectedClassesIfItemBreaks(serial);

            if (!affected.isEmpty()) {
                String[] classOptions = new String[affected.size()];
                for (int i = 0; i < affected.size(); i++) {
                    EquipmentManagementController.ShortageInfo info = affected.get(i);
                    classOptions[i] = info.className + " (" + info.startTimeStr + ") - Needs: " + info.requestedQty;
                }

                String selectedClassStr = (String) JOptionPane.showInputDialog(
                        this,
                        "Equipment Shortage Alert!\nTotal functional items left: " + affected.get(0).availableAfterBreak + "\n\n"
                        + "The following overlapping classes cause a shortage.\n"
                        + "Please select WHICH class you want to adjust:",
                        "Resolve Shortage",
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        classOptions,
                        classOptions[0]);

                if (selectedClassStr == null) {
                    return; 
                }

                int selectedIndex = -1;
                for (int i = 0; i < classOptions.length; i++) {
                    if (classOptions[i].equals(selectedClassStr)) {
                        selectedIndex = i;
                        break;
                    }
                }
                EquipmentManagementController.ShortageInfo selectedInfo = affected.get(selectedIndex);


                Object[] actionOptions = {"Cancel Class", "Reduce Required Equipment", "Abort"};
                int actionChoice = JOptionPane.showOptionDialog(this,
                        "How would you like to handle the class '" + selectedInfo.className + "'?",
                        "Action Required",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        actionOptions,
                        actionOptions[2]);

                if (actionChoice == 0) {
                    TrainingClassController.getInstance().cancelTrainingClass(selectedInfo.classId);
                    JOptionPane.showMessageDialog(this, "Class '" + selectedInfo.className + "' cancelled.");
                } else if (actionChoice == 1) {

                    int newQty = selectedInfo.requestedQty - 1;
                    if (newQty < 0) newQty = 0;
                    TrainingClassController.getInstance().reduceClassEquipmentRequirement(
                            selectedInfo.classId, selectedInfo.typeId, newQty);
                    JOptionPane.showMessageDialog(this, "Equipment requirement reduced for class '" + selectedInfo.className + "'.");
                } else {
                    return; 
                }
            }
        }

        boolean success = isFunctional ? 
                controller.markItemAsFunctional(serial) : 
                controller.markItemAsNonFunctional(serial);

        if (success) {
            controller.markItemAsReviewed(serial);
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Update Failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String serial = (String) model.getValueAt(modelRow, 0);
        String name = (String) model.getValueAt(modelRow, 1);

        List<EquipmentManagementController.ShortageInfo> affectedClasses = 
                controller.getAffectedClassesIfItemBreaks(serial);

        if (!affectedClasses.isEmpty()) {
            StringBuilder msg = new StringBuilder("Cannot delete this item! It will cause an equipment shortage for the following classes:\n\n");
            for (EquipmentManagementController.ShortageInfo info : affectedClasses) {
                msg.append("- Class: ").append(info.className)
                   .append(" (ID: ").append(info.classId).append(")\n")
                   .append("  Needs: ").append(info.requestedQty)
                   .append(", but only ").append(info.availableAfterBreak).append(" will be left.\n\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Delete Blocked", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete item: " + name + " (#" + serial + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.removeItem(serial)) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed (Item might be in use).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importReport() {
        try {
            File f = MONTHLY_REPORT_PATH.toFile();
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "File not found: " + MONTHLY_REPORT_PATH);
                return;
            }
            EquipmentImportController.ImportResult result =
                    EquipmentImportController.getInstance().processMonthlyUpdates(MONTHLY_REPORT_PATH);
            
            JOptionPane.showMessageDialog(this, result.toString(), "Import Result", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= STYLING =================

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(80, 80, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 4) { 
                    String val = (String) value;
                    if (val.contains("YES")) {
                        setForeground(new Color(220, 100, 0)); 
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.LIGHT_GRAY);
                    }
                }
                else if (column == 5) { 
                    String val = (String) value;
                    if ("Functional".equals(val)) {
                        setForeground(new Color(0, 128, 0)); 
                    } else {
                        setForeground(new Color(178, 34, 34)); 
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                } 
                else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 35));
        return btn;
    }
}