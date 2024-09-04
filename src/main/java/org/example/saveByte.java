package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class saveByte extends Component {
    // Сохраняет изменения в файле для выбранной ячейки
    public saveByte(int row, int column, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel, FileChannel fileChannel, JTable hexTable, JLabel decimalValueLabel, JComboBox<String> dataSizeComboBox) {
        try {
            // Получаем позицию в файле для выбранной ячейки
            long address = currentPosition + (long) row * BYTES_PER_LINE + column;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);

            // Получаем размер блока из выпадающего списка
            String dataSize = (String) dataSizeComboBox.getSelectedItem();
            int numBytes = getNumBytes(dataSize);

            // Формируем шестнадцатеричную строку из выбранных ячеек
            StringBuilder hexString = new StringBuilder();
            for (int i = column; i < column + numBytes; i++) {
                String value = (String) tableModel.getValueAt(row, i + 2); // Получаем шестнадцатеричное значение
                if (value != null && !value.isEmpty()) {
                    hexString.append(value);
                } else {
                    hexString.append("00"); // Заполняем пустые ячейки нулями
                }
            }

            // Преобразуем шестнадцатеричную строку в байты
            byte[] bytes = new byte[numBytes];
            for (int i = 0; i < numBytes; i += 2) {
                String hexValue = hexString.substring(i, Math.min(i + 2, hexString.length()));
                bytes[i / 2] = (byte) Integer.parseInt(hexValue, 16);
            }

            // Записываем байты в файл
            fileChannel.write(ByteBuffer.wrap(bytes));

            // Обновляем отображение десятичного значения
            new updateDecimalValue(row, column, dataSizeComboBox, tableModel, decimalValueLabel);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving byte: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    //a
    // Метод для получения количества байт в блоке
    private int getNumBytes(String dataSize) {
        switch (dataSize) {
            case "2 bytes":
                return 2;
            case "4 bytes":
                return 4;
            case "8 bytes":
                return 8;
            default:
                return 0;
        }
    }
}