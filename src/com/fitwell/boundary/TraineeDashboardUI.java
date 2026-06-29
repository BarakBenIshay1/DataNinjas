package com.fitwell.boundary;

import com.fitwell.boundary.login.RoleSelectionUI;
import com.fitwell.control.TraineePortalController;
import com.fitwell.entity.TrainingClass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TraineeDashboardUI extends JFrame {

    private final String traineeId;
    private YearMonth currentMonth;
    private JLabel lblMonthYear;
    private JPanel calendarGrid;
    private List<TrainingClass> monthClasses;
    private TraineePortalController controller = TraineePortalController.getInstance();

    public TraineeDashboardUI(String traineeId) {
        super("FitWell - Trainee Dashboard");
        this.traineeId = traineeId;
        this.currentMonth = YearMonth.now();

        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), 0, getHeight(), new Color(225, 235, 250));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        });
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        JButton btnUnsubscribe = UIBuilder.ButtonBuilder.of("Cancel Subscription").font(new Font("Segoe UI", Font.BOLD, 12))
            .background(new Color(220, 53, 69)).foreground(Color.WHITE).focus(false)
            .cursor(new Cursor(Cursor.HAND_CURSOR)).build();

        btnUnsubscribe.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel subscription?\nYour future classes will be cancelled and your account will be frozen.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.unsubscribeTrainee(traineeId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Subscription cancelled successfully. Your account is now frozen.", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                    new RoleSelectionUI().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "An error occurred. Please contact the manager.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnLogout = UIBuilder.ButtonBuilder.of("Logout")
            .font(new Font("Segoe UI", Font.PLAIN, 12)).background(new Color(255, 240, 240))
            .focus(false).build();
        btnLogout.addActionListener(e -> {
            new RoleSelectionUI().setVisible(true);
            dispose();
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        logoutPanel.setOpaque(false);
        logoutPanel.add(btnUnsubscribe); // הוספנו את כפתור הביטול לצד ה-Logout
        logoutPanel.add(btnLogout);

        String traineeName = controller.getTraineeFullName(traineeId);
        JLabel lblWelcome = new JLabel("Hello, " + (traineeName != null ? traineeName : "Trainee"));
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(30, 55, 100));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setOpaque(false);
        JButton btnPrev = createMonthNavButton("<");
        JButton btnNext = createMonthNavButton(">");
        lblMonthYear = new JLabel();
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMonthYear.setForeground(new Color(60, 60, 60));
        navPanel.add(btnPrev);
        navPanel.add(lblMonthYear);
        navPanel.add(btnNext);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(lblWelcome, BorderLayout.WEST);
        topContainer.add(logoutPanel, BorderLayout.EAST);
        headerPanel.add(topContainer, BorderLayout.NORTH);
        headerPanel.add(navPanel, BorderLayout.CENTER);

        // Stats & Calendar
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);
        contentContainer.setBorder(new EmptyBorder(10, 30, 10, 30));
        contentContainer.add(createStatsPanel()); 
        contentContainer.add(Box.createVerticalStrut(20));

        JPanel daysHeader = new JPanel(new GridLayout(1, 7));
        daysHeader.setOpaque(false);
        daysHeader.setBorder(new EmptyBorder(0, 20, 5, 20));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(100, 100, 100));
            daysHeader.add(lbl);
        }

        calendarGrid = new JPanel(new GridLayout(0, 7, 10, 10));
        calendarGrid.setOpaque(false);
        calendarGrid.setBorder(new EmptyBorder(5, 20, 30, 20));

        JPanel calendarWrapper = new JPanel(new BorderLayout());
        calendarWrapper.setOpaque(false);
        calendarWrapper.add(daysHeader, BorderLayout.NORTH);
        calendarWrapper.add(calendarGrid, BorderLayout.CENTER);
        contentContainer.add(calendarWrapper);

        add(headerPanel, BorderLayout.NORTH);
        add(contentContainer, BorderLayout.CENTER);

        btnPrev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); refreshCalendar(); });
        btnNext.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); refreshCalendar(); });
        refreshCalendar();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(2000, 100));
        panel.setPreferredSize(new Dimension(900, 100));

        int[] stats = controller.getMonthlyStats(traineeId);
        int workouts = stats[0];
        int minutes = stats[1];
        double hours = minutes / 60.0;
        int calories = (int) (hours * 450);

        panel.add(createStateCard("Workouts this Month", String.valueOf(workouts), new Color(100, 149, 237))); 
        panel.add(createStateCard("Hours Trained", String.format("%.1f", hours), new Color(60, 179, 113))); 
        panel.add(createStateCard("Est. Calories Burned", String.valueOf(calories), new Color(255, 127, 80))); 
        return panel;
    }

    private JPanel createStateCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 230), 1),
                new EmptyBorder(10, 20, 10, 20)));
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel lblValue = UIBuilder.LabelBuilder.of(value).font(new Font("Segoe UI", Font.BOLD, 24))
            .foreground(color).build();
        
        JLabel lblLabel = UIBuilder.LabelBuilder.of(label).font(new Font("Segoe UI", Font.BOLD, 12))
            .foreground(Color.GRAY).build();
        textPanel.add(lblValue);
        textPanel.add(lblLabel);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private void refreshCalendar() {
        lblMonthYear.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        monthClasses = controller.getClassesForMonth(currentMonth.getMonthValue(), currentMonth.getYear(), traineeId);
        calendarGrid.removeAll();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); 
        int emptySlots = (dayOfWeek == 7) ? 0 : dayOfWeek; 
        for (int i = 0; i < emptySlots; i++) calendarGrid.add(new JLabel(""));

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            List<TrainingClass> classesToday = getClassesForDate(date);
            DayPanel dayPanel = new DayPanel(day, classesToday);
            dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (!classesToday.isEmpty()) new DayDetailsDialog(TraineeDashboardUI.this, date, classesToday).setVisible(true);
                }
            });
            calendarGrid.add(dayPanel);
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private List<TrainingClass> getClassesForDate(LocalDate date) {
        List<TrainingClass> daily = new ArrayList<>();
        if (monthClasses == null) return daily;
        for (TrainingClass tc : monthClasses) {
            if(tc.getStartDateTime().toLocalDate().equals(date)){
                daily.add(tc);
            }
        }
        return daily;
    }

    private JButton createMonthNavButton(String text) {
        JButton btn = UIBuilder.ButtonBuilder.of(text)
            .font(new Font("Segoe UI", Font.BOLD, 18)).focus(false)
            .border(new LineBorder(new Color(180, 180, 180), 1, true))
            .preferredSize(new Dimension(45, 40)).build();

        btn.setContentAreaFilled(false);
        return btn;
    }

    private class DayPanel extends JPanel {
        public DayPanel(int dayNumber, List<TrainingClass> classes) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblDay = UIBuilder.LabelBuilder.of(" " + dayNumber + " ")
                .font(new Font("Segoe UI", Font.BOLD, 14)).border(new EmptyBorder(5, 5, 0, 0))
                .build();

            if (currentMonth.equals(YearMonth.now()) && dayNumber == LocalDate.now().getDayOfMonth()) {
                lblDay.setForeground(new Color(0, 102, 204));
                lblDay.setText(" " + dayNumber + " (Today)");
            }
            add(lblDay, BorderLayout.NORTH);

            if (!classes.isEmpty()) {
                JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                dotsPanel.setOpaque(false);
                int count = 0;
                for (TrainingClass tc : classes) {
                    if (count >= 4) break; 
                    JLabel dot = new JLabel("●");
                    dot.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    
                    boolean isLate = LocalDateTime.now().isAfter(tc.getStartDateTime());
                    Color foreground = new Color(34, 139, 34);
                    if (isLate || tc.getSeatsLeft() <= 0) foreground = Color.LIGHT_GRAY;
                    dot.setForeground(foreground); 
                    dotsPanel.add(dot);
                    count++;
                }
                add(dotsPanel, BorderLayout.SOUTH);
            }
        }
    }

    // --- DETAILS DIALOG ---
    private class DayDetailsDialog extends JDialog {
        public DayDetailsDialog(JFrame owner, LocalDate date, List<TrainingClass> classes) {
            super(owner, "Classes for " + date.toString(), true);
            setSize(520, 550);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            listPanel.setBackground(new Color(245, 245, 245));

            for (TrainingClass tc: classes) {
                boolean isRegistered = controller.isTraineeRegistered(tc.getClassId(), traineeId);
                
                boolean hasStarted = LocalDateTime.now().isAfter(tc.getStartDateTime());
                boolean hasEnded = LocalDateTime.now().isAfter(tc.getEndDateTime()); 
                boolean isRegistrationClosed = LocalDateTime.now().plusHours(24).isAfter(tc.getStartDateTime());

                JPanel card = new JPanel(new BorderLayout());
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1),
                    new EmptyBorder(10, 10, 10, 10)));
                card.setBackground(Color.WHITE);
                card.setMaximumSize(new Dimension(480, 110));
                
                String startStr = tc.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String endStr = tc.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                
                JPanel infoPanel = new JPanel(new BorderLayout());
                infoPanel.setOpaque(false);
                
                JLabel lblInfo = new JLabel("<html><b style='font-size:12px'>" + startStr + " - " + endStr + "</b><br/>" 
                                            + tc.getName() + " <span style='color:gray'>(" + controller.getClassTypeName(tc.getClassType()) + ")</span>" + 
                                            "<br/>Seats left: <b>" + tc.getSeatsLeft() + "</b></html>");
                infoPanel.add(lblInfo, BorderLayout.CENTER);
                
                // === כפתור הטיפים (💡) ===
                JButton btnTips = new JButton("💡 Tips");
                btnTips.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                btnTips.setFocusPainted(false);
                btnTips.setContentAreaFilled(false);
                btnTips.setBorderPainted(false);
                btnTips.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnTips.setForeground(new Color(0, 102, 204));
                btnTips.setToolTipText("View Class Tips");
                
                btnTips.addActionListener(e -> {
                    List<String> tips = controller.getTipsForClass(tc.getClassId());
                    if (tips.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No specific tips for this class.", "Class Tips", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        StringBuilder sb = new StringBuilder("<html><ul style='margin-left: 10px;'>");
                        for (String tip : tips) {
                            sb.append("<li style='margin-bottom: 5px;'>").append(tip).append("</li>");
                        }
                        sb.append("</ul></html>");
                        JOptionPane.showMessageDialog(this, sb.toString(), "💡 Tips for " + tc.getName(), JOptionPane.PLAIN_MESSAGE);
                    }
                });
                
                infoPanel.add(btnTips, BorderLayout.SOUTH);
                card.add(infoPanel, BorderLayout.CENTER);
                // ===================================

                JButton btnAction = new JButton();
                btnAction.setFocusPainted(false);
                btnAction.setFont(new Font("Segoe UI", Font.BOLD, 12));

                if (hasEnded) {
                    UIBuilder.ButtonBuilder.of(btnAction).text("Completed").enabled(false)
                        .background(new Color(240, 240, 240));
                }
                else if (hasStarted) {
                    UIBuilder.ButtonBuilder.of(btnAction).text("In Session").enabled(false)
                        .background(new Color(255, 230, 150));
                }
                else if (isRegistered) {
                    UIBuilder.ButtonBuilder.of(btnAction).text("Cancel").enabled(true)
                        .background(new Color(220, 53, 69)).foreground(Color.WHITE)
                        .cursor(new Cursor(Cursor.HAND_CURSOR));                    

                    btnAction.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Cancel registration?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                controller.cancelRegistration(traineeId, tc.getClassId());
                                JOptionPane.showMessageDialog(this, "Cancelled.");
                                dispose(); refreshCalendar();
                            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
                        }
                    });
                }
                else if (isRegistrationClosed) {
                    UIBuilder.ButtonBuilder.of(btnAction).text("Closed (<24h)").enabled(false)
                        .background(new Color(230, 230, 230));
                } 
                else if (tc.getSeatsLeft() <= 0) {
                    UIBuilder.ButtonBuilder.of(btnAction).text("Full").enabled(false)
                        .background(new Color(255, 200, 200));
                } 
                else {
                    UIBuilder.ButtonBuilder.of(btnAction).text("Register").enabled(true)
                        .foreground(Color.WHITE).background(new Color(34, 139, 34))
                        .cursor(new Cursor(Cursor.HAND_CURSOR));
                    btnAction.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Register?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                TraineePortalController.getInstance().registerToClass(traineeId, tc.getClassId());
                                JOptionPane.showMessageDialog(this, "Success!");
                                dispose(); refreshCalendar();
                            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                        }
                    });
                }

                JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnWrapper.setOpaque(false);
                btnWrapper.add(btnAction);
                card.add(btnWrapper, BorderLayout.EAST);
                
                listPanel.add(card);
                listPanel.add(Box.createVerticalStrut(10));
            }
            add(new JScrollPane(listPanel), BorderLayout.CENTER);
        }
    }
}