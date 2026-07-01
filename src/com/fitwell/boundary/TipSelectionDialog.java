package com.fitwell.boundary;

import com.fitwell.entity.Tip;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TipSelectionDialog extends JDialog {

    private final List<Tip> selectedTips = new ArrayList<>();
    private final List<TipCard> tipCards = new ArrayList<>();
    private final JLabel lblCounter;
    private final int MAX_TIPS = 5;

    public TipSelectionDialog(JFrame parent, List<Tip> tips) {
        super(parent, "Select Daily Tips", true);
        setSize(550, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // רקע כללי
        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("Personalize Your Plan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(30, 55, 100));

        JLabel subtitle = new JLabel("Choose the tips that motivate you most (Max " + MAX_TIPS + ")");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(80, 80, 80));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        // ===== LIST (SCROLL PANE) =====
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        for (Tip t : tips) {
            TipCard card = new TipCard(t);
            tipCards.add(card);
            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(10)); // רווח בין כרטיסים
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ===== FOOTER =====
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 25, 20, 25));

        // מונה בחירות
        lblCounter = new JLabel("0/" + MAX_TIPS + " Selected");
        lblCounter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCounter.setForeground(new Color(100, 100, 100));

        // כפתור אישור
        JButton btnConfirm = new JButton("Save Selection");
        styleButton(btnConfirm);
        btnConfirm.addActionListener(e -> {
            selectedTips.clear();
            for (TipCard card : tipCards) {
                if (card.isSelected()) {
                    selectedTips.add(card.getTip());
                }
            }
            dispose(); // סגירה
        });

        footerPanel.add(lblCounter, BorderLayout.WEST);
        footerPanel.add(btnConfirm, BorderLayout.EAST);

        // הרכבה
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    // פונקציה לעדכון המונה והגבלת הבחירה
    private void updateSelectionState() {
        int count = 0;
        for (TipCard card : tipCards) {
            if (card.isSelected()) count++;
        }

        lblCounter.setText(count + "/" + MAX_TIPS + " Selected");

        // אם הגענו למקסימום - נועלים את אלו שלא נבחרו
        boolean reachedMax = (count >= MAX_TIPS);
        for (TipCard card : tipCards) {
            if (!card.isSelected()) {
                card.setEnabledState(!reachedMax);
            }
        }
    }

    public List<Tip> getSelectedTips() {
        return selectedTips;
    }

    // ==========================================
    // INNER CLASS: TIP CARD (עיצוב הכרטיסייה)
    // ==========================================
    private class TipCard extends JPanel {
        private final Tip tip;
        private final JCheckBox checkBox;
        private boolean isEnabled = true;

        public TipCard(Tip tip) {
            this.tip = tip;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1, true), // מסגרת עגולה ועדינה
                    new EmptyBorder(10, 10, 10, 10)
            ));
            setMaximumSize(new Dimension(500, 80)); // גובה קבוע לכל טיפ

            checkBox = new JCheckBox();
            checkBox.setOpaque(false);
            
            // טקסט הטיפ
            JTextArea textArea = new JTextArea(tip.getContent());
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setOpaque(false);
            textArea.setHighlighter(null);
            textArea.setForeground(new Color(50, 50, 50));

            add(checkBox, BorderLayout.WEST);
            add(textArea, BorderLayout.CENTER);

            // לחיצה על כל הפאנל תפעיל את הצ'קבוקס
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isEnabled) {
                        checkBox.setSelected(!checkBox.isSelected());
                        updateVisuals();
                        updateSelectionState();
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled) setBackground(new Color(245, 250, 255));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (isEnabled) updateVisuals();
                }
            };
            
            addMouseListener(ma);
            textArea.addMouseListener(ma); // גם לחיצה על הטקסט תעבוד
            checkBox.addActionListener(e -> {
                updateVisuals();
                updateSelectionState();
            });
        }

        private void updateVisuals() {
            if (checkBox.isSelected()) {
                setBackground(new Color(230, 240, 255)); // כחול בהיר כשנבחר
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(100, 149, 237), 2, true), // מסגרת כחולה מודגשת
                        new EmptyBorder(9, 9, 9, 9)
                ));
            } else {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(220, 220, 220), 1, true),
                        new EmptyBorder(10, 10, 10, 10)
                ));
            }
        }

        public void setEnabledState(boolean state) {
            this.isEnabled = state;
            checkBox.setEnabled(state);
            // אפקט ויזואלי לנעילה (אפור)
            if (!state) {
                setBackground(new Color(245, 245, 245));
                getComponents()[1].setForeground(Color.GRAY); // טקסט אפור
            } else {
                setBackground(Color.WHITE);
                getComponents()[1].setForeground(new Color(50, 50, 50));
            }
        }

        public boolean isSelected() { return checkBox.isSelected(); }
        public Tip getTip() { return tip; }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(34, 139, 34)); // ירוק
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
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