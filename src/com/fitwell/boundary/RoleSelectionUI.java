package com.fitwell.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoleSelectionUI extends JFrame {

    public RoleSelectionUI() {

        setTitle("FitWell – Role Selection");

        setSize(520, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new AppBackgroundPanel());
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JLabel title = new JLabel("FITWELL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(30, 55, 100));

        JLabel subtitle = new JLabel(
                "Choose your role to continue",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(80, 80, 80));
        
        JPanel header = UIBuilder.PanelBuilder.of(new GridLayout(2, 1, 0, 6))
        	.border(BorderFactory.createEmptyBorder(30, 10, 20, 10)).opaque(false)
        	.add(title).add(subtitle).build();

        // roles
        JButton btnConsultant = createRoleButton("🧑‍🏫","Fitness Consultant","");
        JButton btnTrainee = createRoleButton("🏃","Trainee","");
        JButton btnManager = createRoleButton("👔","Manager","");

        // actions
        btnConsultant.addActionListener(e -> {
            new ConsultantLoginUI().setVisible(true);
            dispose();
        });

        btnTrainee.addActionListener(e -> {
            new TraineeLoginUI().setVisible(true);
            dispose();
        });

        btnManager.addActionListener(e -> {
            new ManagerLoginUI().setVisible(true);
            dispose();
        });

        JPanel center = UIBuilder.PanelBuilder.of()
        		.opaque(false).boxLayout(BoxLayout.Y_AXIS)
        		.add(btnConsultant).add(Box.createVerticalStrut(20)).add(btnTrainee)
        		.add(Box.createVerticalStrut(20)).add(btnManager)
        		.build();

        JPanel centerWrapper = UIBuilder.PanelBuilder.of()
        		.opaque(false).border(BorderFactory.createEmptyBorder(10, 0, 30, 0))
        		.add(center).build();

        add(header, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    // button factory
    private JButton createRoleButton(String emoji,String title,String subtitle) {
    	String html = "<html><center style='font-family:Segoe UI'>"
                        + "<div style='font-size:34px; margin-bottom:6px'>" + emoji + "</div>"
                        + "<b style='font-size:15px'>" + title + "</b><br>"
                        + "<span style='font-size:12px;color:#555'>" + subtitle + "</span>"
                        + "</center></html>";

    	JButton btn = UIBuilder.ButtonBuilder.of(html)
    			.preferredSize(new Dimension(360, 110)).background(new Color(235, 242, 255))
    			.focus(false).opaque(true).border(BorderFactory.createEmptyBorder(18, 10, 18, 10))
    			.build();
        
        btn.setMaximumSize(new Dimension(360, 110));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(220, 232, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(235, 242, 255));
            }
        });

        return btn;
    }

    // ===== APP BACKGROUND =====
    private static class AppBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(247, 250, 255),
                    0, getHeight(), new Color(215, 225, 235)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
