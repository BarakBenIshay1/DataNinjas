package com.fitwell.boundary.reports;

import com.fitwell.control.ReportController;
import com.fitwell.entity.EquipmentReportItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class InventoryReportForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public InventoryReportForm() {
        setTitle("Equipment Inventory & Usage Report");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // כותרת
        JLabel lblTitle = new JLabel("Yearly Equipment Usage Report", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // טבלה
        String[] columns = {"Equipment Name", "Category", "Times Used (This Year)"};
        model = new DefaultTableModel(columns, 0) {
            @Override // הטבלה לקריאה בלבד
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        loadData(); // טעינת נתונים

        add(new JScrollPane(table), BorderLayout.CENTER);

        // כפתור יצוא
        JButton btnExport = new JButton("Export to XML (SwiftFit)");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(Color.WHITE);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(btnExport);
        add(bottomPanel, BorderLayout.SOUTH);

        // לוגיקה לכפתור
        btnExport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save XML Report");
            fileChooser.setSelectedFile(new File("SwiftFit_Inventory_Report.xml"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                boolean success = ReportController.getInstance().exportReportToXML(fileToSave.getAbsolutePath());
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Report exported successfully!\nLocation: " + fileToSave.getAbsolutePath(), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to export report.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadData() {
        List<EquipmentReportItem> data = ReportController.getInstance().getEquipmentInventoryReportData();
        model.setRowCount(0); // איפוס
        for (EquipmentReportItem item : data) {
            model.addRow(new Object[]{
                item.getTypeName(),
                item.getCategory(),
                item.getTimesUsed()
            });
        }
    }
}