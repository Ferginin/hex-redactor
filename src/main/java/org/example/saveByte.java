package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class saveByte extends Component {
    // Сохраняет изменения в файле для выбранной ячейки
    public saveByte(int row, int column, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel, FileChannel fileChannel, JTable hexTable) {
        try {
            // Получаем позицию в файле для выбранной ячейки
            long address = currentPosition + (long) row * BYTES_PER_LINE + column;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);
            // Получаем значение из ячейки
            String value = (String) tableModel.getValueAt(row, column + 2);
            if (value == null || value.isEmpty()) {
                value = "00"; // Сохраняем 0, если пусто
            }
            // Записываем значение в файл
            byte byteValue;
            try {
                byteValue = Byte.parseByte(value.trim(), 16); // Проверяем, является ли значение hex числом
            } catch (NumberFormatException e) {
                byteValue = 0; // Записываем 0, если невозможно преобразовать
            }
            fileChannel.write(ByteBuffer.wrap(new byte[]{byteValue}));
            // Обновляем данные в ячейке
            new updateRow(row, BYTES_PER_LINE, currentPosition, tableModel, fileChannel, hexTable);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving byte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
