package com.fitwell.boundary;

import com.fitwell.control.ManagerController;
import com.fitwell.control.ManagerController.PairData;
import com.fitwell.entity.Trainee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class EditTraineeListForm extends JDialog {

    private JTextField txtSearch;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtId, txtFirst, txtLast, txtEmail, txtPhone;
    private JSpinner dateSpinner;
    private JComboBox<PairData> cmbUpdateMethod;
    private JCheckBox chkActive;
    private JButton btnUpdate;

    private ManagerController controller = ManagerController.getInstance();

    public EditTraineeListForm(Frame owner) {
        super(owner, "Manage Trainees", true);

        setSize(700, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Search & Edit Trainees");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 55, 100));
        topPanel.add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search (Name or ID): "), BorderLayout.WEST);
        txtSearch = new JTextField();
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] cols = { "ID", "First Name", "Last Name" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(248, 250, 255));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Trainee ID (Locked):"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(15);
        txtId.setEditable(false);
        txtId.setBackground(new Color(230, 230, 230));
        formPanel.add(txtId, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Birth Date:"), gbc);
        gbc.gridx = 3;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        txtFirst = new JTextField(15);
        formPanel.add(txtFirst, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3;
        txtLast = new JTextField(15);
        formPanel.add(txtLast, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 3;
        txtPhone = new JTextField(15);
        formPanel.add(txtPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Update Method:"), gbc);
        gbc.gridx = 1;
        cmbUpdateMethod = new JComboBox<PairData>();
        for (PairData pd : controller.getUpdateMethods()) {
            cmbUpdateMethod.addItem(pd);
        }
        formPanel.add(cmbUpdateMethod, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Account Status:"), gbc);
        gbc.gridx = 3;
        chkActive = new JCheckBox("Active / Allowed to Login");
        chkActive.setBackground(new Color(248, 250, 255));
        formPanel.add(chkActive, gbc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionPanel.setOpaque(false);

        btnUpdate = new JButton("Update Details");
        btnUpdate.setBackground(new Color(0, 102, 204));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.setEnabled(false);

        JButton btnClose = new JButton("Back to Dashboard");
        btnClose.setBackground(new Color(100, 100, 100));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        actionPanel.add(btnUpdate);
        actionPanel.add(btnClose);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 5, 0, 5);
        formPanel.add(actionPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.SOUTH);

        loadTraineesToTable();
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1, 2));
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int viewRow = table.getSelectedRow();
                int modelRow = table.convertRowIndexToModel(viewRow);
                int id = (int) model.getValueAt(modelRow, 0);

                Trainee trainee = controller.getTraineeDetails(id);
                if (trainee != null) {
                    txtId.setText(trainee.getTraineeId() + "");
                    txtFirst.setText(trainee.getFirstName());
                    txtLast.setText(trainee.getLastName());
                    txtEmail.setText(trainee.getEmail());
                    txtPhone.setText(trainee.getPhoneNumber());
                    Date date = Date.from(trainee.getBirthDate().atZone(ZoneId.systemDefault()).toInstant());
                    dateSpinner.setValue(date);
                    cmbUpdateMethod.setSelectedItem(controller.getUpdateMethod(trainee.getUpdateMethod()));
                    chkActive.setSelected(trainee.isActive());

                    btnUpdate.setEnabled(true);
                }
            } else {
                btnUpdate.setEnabled(false);
            }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtId.getText().trim());

                String fName = txtFirst.getText().trim();
                String lName = txtLast.getText().trim();
                if (fName.isEmpty() || lName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "First and Last names cannot be empty.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String email = txtEmail.getText().trim();
                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "email cannot be empty.", "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String phone = txtPhone.getText().trim();
                if (!phone.matches("05\\d{8}|0\\d{8}")) {
                    JOptionPane.showMessageDialog(this, "Invalid phone number.", "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date utilDate = (Date) dateSpinner.getValue();
                if (utilDate.after(new Date())) {
                    JOptionPane.showMessageDialog(this, "Birth date cannot be in the future.", "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                java.sql.Date birthDate = new java.sql.Date(utilDate.getTime());
                PairData updateMethod = (PairData) cmbUpdateMethod.getSelectedItem();
                boolean isActive = chkActive.isSelected();

                boolean success = controller.updateTraineeDetails(id, fName, lName, email, phone, birthDate,
                        updateMethod.getId(), isActive);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Trainee details updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadTraineesToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred during update.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadTraineesToTable() {
        model.setRowCount(0);
        List<Object[]> trainees = controller.getAllTrainees();
        for (Object[] t : trainees) {
            model.addRow(new Object[] { t[0], t[1], t[2] });
        }
    }
}