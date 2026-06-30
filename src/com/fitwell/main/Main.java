package com.fitwell.main;

import java.awt.Color;

import javax.swing.UIManager;

import com.fitwell.boundary.login.RoleSelectionUI;
import com.formdev.flatlaf.themes.FlatMacLightLaf;


public class Main {
    public static void main(String[] args) {
        

        try {
            
            // round corners
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            
            // set focus border
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.innerFocusWidth", 1);
            
            // disable focus border on buttons
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            
            // theme
            FlatMacLightLaf.setup();
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize themed GUI");
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            new RoleSelectionUI().setVisible(true);
        });
    }
}