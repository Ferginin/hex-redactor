package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class updateRow extends Component {

    // Обновляет строку таблицы по выбранному индексу
    public updateRow(int row, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel, FileChannel fileChannel, JTable hexTable) {
        try {
            // Получаем позицию в файле для выбранной строки
            long address = currentPosition + (long) row * BYTES_PER_LINE;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            // Читаем данные из файла
            fileChannel.read(buffer);
            buffer.flip();

            // Обновляем данные в строке таблицы
            for (int i = 0; i < BYTES_PER_LINE; i++) {
                if (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    tableModel.setValueAt(String.format("%02X", b), row, i + 2);
                } else {
                    tableModel.setValueAt("00", row, i + 2); // Заполняем ячейку нулями, если данных нет
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
