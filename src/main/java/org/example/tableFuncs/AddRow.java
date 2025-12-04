package org.example.tableFuncs;

import javax.swing.table.DefaultTableModel;
import java.nio.ByteBuffer;

public class AddRow {
    private int rowNumber; // Начальный номер строки

    public AddRow(int currentPage) {
        this.rowNumber = (currentPage) * 100 + 1;
    }

    // Добавляет строку данных в таблицу
    public void add(ByteBuffer buffer, int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel) {
        try {
            String[] rowData;
            rowData = new String[BYTES_PER_LINE + 3];
            rowData[0] = String.valueOf(rowNumber++); // Нумерация строк
            rowData[1] = String.format("%08X", currentPosition);
            StringBuilder hexString = new StringBuilder();
            int bytesRead = buffer.remaining();
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer.get();
                rowData[i + 3] = String.format("%02X", b);
                hexString.append(String.format("%02X ", b));
            }
            rowData[2] = hexString.toString();
            tableModel.addRow(rowData);
        } catch (Exception e) {
            System.err.println("Error adding row: " + e.getMessage());
        }
    }
}