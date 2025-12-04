package org.example.tableSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TextAreaHandler {

    public static void updateTextArea(DefaultTableModel tableModel, JTextArea textArea) {

        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int column = 3; column < tableModel.getColumnCount(); column++) {
                String hexValue = (String) tableModel.getValueAt(row, column);
                if (hexValue != null && hexValue.length() == 2) {
                    try {
                        int byteValue = Integer.parseInt(hexValue, 16);
                        if (byteValue >= 32 && byteValue <= 126) { // Проверка на печатные символы
                            sb.append((char) byteValue);
                        } else {
                            sb.append('.'); // Заменяем непечатные символы на точки
                        }
                    } catch (NumberFormatException e) {
                        sb.append('.'); // Заменяем некорректные значения на точки
                    }
                } else {
                    sb.append('.'); // Заменяем пустые или некорректные значения на точки
                }
            }
        }
        try{
            textArea.setText(sb.toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}