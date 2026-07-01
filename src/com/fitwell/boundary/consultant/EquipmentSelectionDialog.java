package com.fitwell.boundary.consultant;

import com.fitwell.control.TrainingClassController;
import com.fitwell.control.TrainingClassController.EquipmentTypeView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentSelectionDialog extends JDialog {

    private final Map<Integer, Integer> selectedEquipment = new HashMap<>();
    private final List<EquipmentRowPanel> rowPanels = new ArrayList<>();
    
    private final LocalDateTime start;
    private final LocalDateTime end;
    private TrainingClassController controller = TrainingClassController.getInstance();

    public EquipmentSelectionDialog(Window owner, LocalDateTime start, LocalDateTime end) {
        super(owner, "Select Equipment", ModalityType.APPLICATION_MODAL);
        this.start = start;
        this.end = end;

        setSize(550, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // רקע
        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("Class Equipment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 55, 100));

        // פורמט שעות יפה (למשל: 18:00 - 19:00)
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        JLabel subtitle = new JLabel("Availability for: " + start.format(timeFmt) + " - " + end.format(timeFmt));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(80, 80, 80));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // ===== LIST (SCROLL PANE) =====
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBorder(new EmptyBorder(10, 20, 10, 20));

        // טעינת הנתונים
        loadData(listContainer);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ===== FOOTER =====
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(200, 60, 60));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Confirm Selection");
        styleButton(btnSave, new Color(34, 139, 34));
        btnSave.addActionListener(e -> saveAndClose());

        footerPanel.add(btnCancel);
        footerPanel.add(btnSave);

        // הרכבה
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadData(JPanel container) {
        List<EquipmentTypeView> availabilityData = controller.getAvailabilityReportForTime(start, end);

        for (EquipmentTypeView row : availabilityData) {
            int id = row.getEquipmentTypeID();
            String name = row.getName();
            int available = row.getAvailable();

            EquipmentRowPanel panel = new EquipmentRowPanel(id, name, available);
            rowPanels.add(panel);
            container.add(panel);
            container.add(Box.createVerticalStrut(10));
        }
    }

    private void saveAndClose() {
        selectedEquipment.clear();
        
        for (EquipmentRowPanel panel : rowPanels) {
            int qty = panel.getSelectedQuantity();
            if (qty > 0) {
                selectedEquipment.put(panel.getTypeId(), qty);
            }
        }
        // אין צורך בבדיקות שגיאה כי הספינר מגביל את המשתמש אוטומטית!
        dispose();
    }

    public Map<Integer, Integer> getSelectedEquipment() {
        return selectedEquipment;
    }

    // ==========================================
    // INNER CLASS: EQUIPMENT ROW (העיצוב של כל שורה)
    // ==========================================
    private class EquipmentRowPanel extends JPanel {
        private final int typeId;
        private final JSpinner quantitySpinner;

        public EquipmentRowPanel(int typeId, String name, int available) {
            this.typeId = typeId;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(10, 15, 10, 15)
            ));
            setMaximumSize(new Dimension(600, 60)); // גובה קבוע

            // צד שמאל: שם + זמינות
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JLabel lblAvailable;
            if (available > 0) {
                lblAvailable = new JLabel("Available: " + available);
                lblAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblAvailable.setForeground(new Color(0, 100, 0)); // ירוק כהה
            } else {
                lblAvailable = new JLabel("Out of Stock");
                lblAvailable.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblAvailable.setForeground(Color.RED);
                lblName.setForeground(Color.GRAY); // אפור אם אין במלאי
            }
            
            infoPanel.add(lblName);
            infoPanel.add(lblAvailable);

            // צד ימין: ספינר לכמות
            SpinnerModel model = new SpinnerNumberModel(0, 0, available, 1); // מינימום 0, מקסימום available
            quantitySpinner = new JSpinner(model);
            quantitySpinner.setPreferredSize(new Dimension(60, 30));
            
            // עיצוב הספינר
            JComponent editor = quantitySpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) editor).getTextField().setHorizontalAlignment(JTextField.CENTER);
            }

            // אם אין מלאי - נועלים
            if (available == 0) {
                quantitySpinner.setEnabled(false);
            }

            add(infoPanel, BorderLayout.CENTER);
            add(quantitySpinner, BorderLayout.EAST);
        }

        public int getTypeId() { return typeId; }
        
        public int getSelectedQuantity() {
            return (int) quantitySpinner.getValue();
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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