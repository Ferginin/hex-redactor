package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// Открывает файл с помощью диалогового окна
public class openFile extends Component {

    public openFile(DefaultTableModel tableModel, FileChannel fileChannel, long fileSize, long currentPosition, JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Закрываем предыдущий файл, если он открыт
                if (fileChannel != null) {
                    fileChannel.close();
                }
                // Открываем выбранный файл
                Path file = fileChooser.getSelectedFile().toPath();
                fileChannel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
                fileSize = fileChannel.size();
                currentPosition = 0;

                // Очищаем таблицу и текст
                tableModel.setRowCount(0);
                textArea.setText("");

                // Заполняем таблицу данными
                new updateTable(tableModel, 16, currentPosition, fileSize, fileChannel);

                // Загружаем текст из файла
                try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    StringBuilder text = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        text.append(line).append("\n");
                    }
                    textArea.setText(text.toString());
                }

                // Заполняем таблицу данными
                new updateTable(tableModel, 16, currentPosition, fileSize, fileChannel);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                // Если возникает ошибка, не обновляем fileChannel, чтобы избежать ошибки "No file open"
                fileChannel = null;
            }
        }
    }
}
//a
