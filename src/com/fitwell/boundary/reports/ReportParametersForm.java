package com.fitwell.boundary.reports;

import com.fitwell.control.ReportController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class ReportParametersForm extends JDialog {

    private final JSpinner startSpinner;
    private final JSpinner endSpinner;
    private final JProgressBar progressBar; 
    private final JButton btnGenerate;
    private final JButton btnCancel;

    public ReportParametersForm(Frame owner) {
        super(owner, "Generate Report", true);

        setSize(500, 420); 
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new AppBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 30, 10, 30));

        JLabel title = new JLabel("Low Participation Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 55, 100));

        JLabel subtitle = new JLabel("Select the date range for the PDF report");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 100, 100));

        JPanel textWrapper = new JPanel(new GridLayout(2, 1));
        textWrapper.setOpaque(false);
        textWrapper.add(title);
        textWrapper.add(subtitle);

        headerPanel.add(textWrapper, BorderLayout.WEST);

        // === FORM (CENTER) ===
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        startSpinner = createDateTimeSpinner();
        endSpinner = createDateTimeSpinner();

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);    
        progressBar.setPreferredSize(new Dimension(200, 6)); 
        progressBar.setForeground(new Color(34, 139, 34)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0;

        cardPanel.add(createLabel("Start Date:"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        cardPanel.add(startSpinner, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 5, 0);
        cardPanel.add(createLabel("End Date:"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        cardPanel.add(endSpinner, gbc);

        gbc.gridy++;
        cardPanel.add(progressBar, gbc);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(10, 0, 0, 0));
        centerWrapper.add(cardPanel);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 25, 10, 25));

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(200, 60, 60)); 
        btnCancel.addActionListener(e -> dispose());

        btnGenerate = new JButton("Generate Report");
        styleButton(btnGenerate, new Color(34, 139, 34)); 
        btnGenerate.addActionListener(e -> onGenerate());

        footerPanel.add(btnCancel);
        footerPanel.add(btnGenerate);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }


    private void onGenerate() {
        Date from = (Date) startSpinner.getValue();
        Date to = (Date) endSpinner.getValue();

        if (to.before(from)) {
            JOptionPane.showMessageDialog(this, "End date must be after Start date.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnGenerate.setEnabled(false);
        btnCancel.setEnabled(false);
        progressBar.setVisible(true);  
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(1000); 
                
                ReportController.getInstance().generateLowParticipationReport(from, to);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    
                    JOptionPane.showMessageDialog(ReportParametersForm.this,
                            "Report created successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();

                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ReportParametersForm.this,
                            "Error generating PDF:\n" + ex.getCause().getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    
                    btnGenerate.setEnabled(true);
                    btnCancel.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private JSpinner createDateTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);
        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)editorComponent).getTextField();
            tf.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setHorizontalAlignment(JTextField.CENTER);
        }
        spinner.setBorder(new LineBorder(new Color(200, 200, 200)));
        spinner.setPreferredSize(new Dimension(250, 35));
        return spinner;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(80, 80, 80));
        return l;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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