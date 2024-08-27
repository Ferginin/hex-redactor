package org.example;

import javax.swing.table.DefaultTableModel;
import java.nio.ByteBuffer;

public class addRow {

    // Добавляет строку данных в таблицу
    public addRow(ByteBuffer buffer, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel) {
        String[] rowData = new String[BYTES_PER_LINE + 3];
        rowData[0] = String.valueOf(tableModel.getRowCount() + 1); // Нумерация строк
        rowData[1] = String.format("%08X", currentPosition);
        StringBuilder hexString = new StringBuilder();
        int bytesRead = buffer.remaining();
        for (int i = 0; i < bytesRead; i++) {
            byte b = buffer.get();
            rowData[i + 3] = String.format("%02X", b);
            hexString.append(String.format("%02X ", b));
        }
        rowData[2] = hexString.toString();
        // Заполняем пустые ячейки нулями
        for (int i = bytesRead; i < BYTES_PER_LINE; i++) {
            rowData[i + 2] = "00"; // Fill with zeros
        }
        tableModel.addRow(rowData);
    }
}
