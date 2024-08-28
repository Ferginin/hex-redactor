package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class updateDecimalValue {
    // Обновляет отображение десятичного значения выделенного байта
    public updateDecimalValue(int row, int column, DefaultTableModel tableModel, JLabel decimalValueLabel) {
        String value = (String) tableModel.getValueAt(row, column + 2); // Получаем hex значение
        if (value != null && !value.isEmpty()) {
            try {
                int intValue = Integer.parseUnsignedInt(value.trim(), 16); // Преобразуем hex в беззнаковое значение
                byte byteValue = (byte) intValue; // Преобразуем в байт
                decimalValueLabel.setText(String.format("Decimal Value (Signed): %d, (Unsigned): %d", byteValue, intValue));
            } catch (NumberFormatException e) {
                decimalValueLabel.setText("Decimal Value: Invalid");
            }
        } else {
            decimalValueLabel.setText("Decimal Value: Invalid");
        }
    }
}
