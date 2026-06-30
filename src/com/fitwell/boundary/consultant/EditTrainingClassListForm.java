package com.fitwell.boundary.consultant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
import com.fitwell.control.TrainingClassController;

public class EditTrainingClassListForm extends JDialog {

    private final DefaultTableModel model;
    private final JTable table;
    private final JButton btnEdit;
    private TableRowSorter<DefaultTableModel> tableSorter; 

    public EditTrainingClassListForm(Frame owner) {
        super(owner, "Edit Training Classes", true); 

        setSize(900, 550); 
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));
        JLabel title = UIBuilder.LabelBuilder.of("Training Classes Management")
        	.font(new Font("Segoe UI", Font.BOLD, 26)).foreground(new Color(30, 55, 100))
        	.build();
        JLabel subtitle = UIBuilder.LabelBuilder.of("Select an active class to edit")
            	.fontRegular().foreground(new Color(100, 100, 100))
            	.build();        	

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(title);
        textWrapper.add(subtitle);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        JLabel lblSearch = UIBuilder.LabelBuilder.of("🔍 Search Class Name: ").fontRegular().build();
        
        JTextField txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);

        headerPanel.add(textWrapper, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        String[] columns = {
            "Class ID", "Name", "Start Time", "End Time", "Type", "Max Participants", "Status"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(model);
        styleTable(table); 

        tableSorter = new TableRowSorter<>(model);
        table.setRowSorter(tableSorter);

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
                    tableSorter.setRowFilter(null); 
                } else {
                    tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 25, 0, 25),
                new LineBorder(new Color(230, 230, 230), 1)
        ));

        btnEdit = new JButton("Edit Selected Class");
        JButton btnRefresh = new JButton("Refresh List");
        JButton btnClose = new JButton("Close");

        styleButton(btnEdit, new Color(34, 139, 34));   
        styleButton(btnRefresh, new Color(70, 130, 180)); 
        styleButton(btnClose, new Color(100, 100, 100));  
        
        btnEdit.setEnabled(false);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 25, 10, 25));
        
        bottom.add(btnClose);
        bottom.add(btnRefresh);
        bottom.add(btnEdit);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                checkSelectionAndToggleEditButton();
            }
        });

        btnClose.addActionListener(e -> dispose());
        btnRefresh.addActionListener(e -> refreshTable());

        btnEdit.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;

            int modelRow = table.convertRowIndexToModel(viewRow);

            Object[] data = new Object[table.getColumnCount()];
            for (int i = 0; i < data.length; i++) {
                data[i] = model.getValueAt(modelRow, i);
            }

            EditTrainingClassForm dlg = new EditTrainingClassForm(this, data);
            dlg.setVisible(true);

            refreshTable();
            txtSearch.setText("");
        });

        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0); 
        var classes = TrainingClassController.getInstance().getAllTrainingClasses();
        for (Object[] row : classes) {
            model.addRow(row); 
        }
        btnEdit.setEnabled(false);
    }
    
    private void checkSelectionAndToggleEditButton() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            btnEdit.setEnabled(false);
            btnEdit.setToolTipText("Select a class to edit");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        java.sql.Timestamp startTs = (java.sql.Timestamp) model.getValueAt(modelRow, 2);
        String status = (String) model.getValueAt(modelRow, 6);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        boolean isEditable = startTs != null && 
                             startTs.toLocalDateTime().isAfter(now) && 
                             !"Cancelled".equals(status) && 
                             !"Completed".equals(status);

        btnEdit.setEnabled(isEditable);
        if (isEditable) {
            btnEdit.setToolTipText("Edit this class details and equipment");
        } else {
            btnEdit.setToolTipText("Cannot edit past, cancelled, or completed classes");
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 242, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(242, 242, 242));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(new Color(70, 70, 70));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
                l.setPreferredSize(new Dimension(l.getPreferredSize().width, 40));
                return l;
            }
        });
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                int modelRow = table.convertRowIndexToModel(row);
                java.sql.Timestamp startTs = (java.sql.Timestamp) model.getValueAt(modelRow, 2);
                String status = (String) model.getValueAt(modelRow, 6);
                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                boolean isEditable = startTs != null && 
                                     startTs.toLocalDateTime().isAfter(now) && 
                                     !"Cancelled".equals(status) && 
                                     !"Completed".equals(status);
                
                if (!isSelected) {
                    if (!isEditable) {
                        c.setForeground(Color.GRAY);
                        c.setBackground(new Color(248, 248, 248)); 
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }

    private void styleButton(JButton btn, Color bg) {
    	btn = UIBuilder.ButtonBuilder.of(btn).font(new Font("Segoe UI", Font.BOLD, 14))
    	.background(bg).foreground(Color.WHITE).focus(false)
    	.border(BorderFactory.createEmptyBorder(10, 20, 10, 20))
    	.cursor(new Cursor(Cursor.HAND_CURSOR))
    	.build();

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