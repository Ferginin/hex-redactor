package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
//a
public class updateTable extends Component {

    // Обновляет таблицу данными из файла
    public updateTable(DefaultTableModel tableModel, int BYTES_PER_LINE, long currentPosition, long fileSize, FileChannel fileChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            while (currentPosition < fileSize) {
                int bytesRead = fileChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                buffer.flip();
                new addRow(buffer, 16, currentPosition, tableModel);
                buffer.clear();
                currentPosition += bytesRead;
            }
            // После заполнения добавляем пустые строки, если это необходимо
            while (currentPosition % BYTES_PER_LINE != 0) {
                String[] rowData = new String[BYTES_PER_LINE + 3];
                rowData[0] = ""; // Номера строк автоматически обновятся
                rowData[1] = String.format("%08X", currentPosition);
                rowData[2] = "";  // Hex column
                Arrays.fill(rowData, 3, BYTES_PER_LINE + 3, "00"); // Fill with zeros
                tableModel.addRow(rowData);
                currentPosition++;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
