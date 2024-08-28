package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class saveByte extends Component {
    // Сохраняет изменения в файле для выбранной ячейки
    public saveByte(int row, int column, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel, FileChannel fileChannel, JTable hexTable, JLabel decimalValueLabel) {
        try {
            // Получаем позицию в файле для выбранной ячейки
            long address = currentPosition + (long) row * BYTES_PER_LINE + column;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);

            // Получаем значение из ячейки
            String value = (String) tableModel.getValueAt(row, column + 2);

            if (value == null || value.isEmpty()) {
                value = "00"; // Сохраняем 0, если ячейка пустая
            }

            int intValue; // Объявляем переменную для целого значения
            try {
                // Преобразуем строку из hex в целое без знака
                intValue = Integer.parseUnsignedInt(value.trim(), 16);
            } catch (NumberFormatException e) {
                intValue = 0; // Записываем 0, если невозможно преобразовать
            }

            byte byteValue = (byte) intValue; // Преобразуем в байт
            // Записываем значение в файл, используя ByteBuffer
            fileChannel.write(ByteBuffer.wrap(new byte[]{byteValue}));

            // Обновляем данные в ячейке
            new updateRow(row, BYTES_PER_LINE, currentPosition, tableModel, fileChannel, hexTable);
            // Обновляем отображение десятичного значения
            new updateDecimalValue(row, column, tableModel, decimalValueLabel);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving byte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
