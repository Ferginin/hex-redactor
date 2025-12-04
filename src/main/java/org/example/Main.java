package org.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(HexEditor::new);
        } catch (Exception e) {
            System.err.println("Error initializing application: " + e.getMessage());
        }
    }
}