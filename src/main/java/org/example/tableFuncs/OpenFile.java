package org.example.tableFuncs;

import org.example.HexEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OpenFile extends Component {

    private FileChannel fileChannel;
    private long fileSize;

    public OpenFile(FileChannel fileChannel, long fileSize) {
        this.fileChannel = fileChannel;
        this.fileSize = fileSize;
    }

    public void open(DefaultTableModel tableModel, JTextArea textArea, HexEditor hexEditor) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Закрываем предыдущий файл, если он открыт
                if (this.fileChannel != null) {
                    this.fileChannel.close();
                }
                // Открываем выбранный файл для чтения и записи
                Path file = fileChooser.getSelectedFile().toPath();
                this.fileChannel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);

                if (this.fileChannel != null) {
                    this.fileSize = this.fileChannel.size();

                    // Читаем весь файл в память
                    byte[] fileContent = new byte[(int) this.fileSize];
                    ByteBuffer buffer = ByteBuffer.wrap(fileContent);
                    this.fileChannel.read(buffer);

                    // Сохраняем содержимое файла в HexEditor
                    hexEditor.setFileContent(fileContent);

                    // Очищаем таблицу и текст
                    tableModel.setRowCount(0);
                    textArea.setText("");
                    int currentPage = 0;

                    // Обновляем размер файла и состояние кнопок навигации
                    hexEditor.setFileSize(this.fileSize);
                    hexEditor.loadPage(currentPage);
                    hexEditor.updateCurrentPageLabel(currentPage + 1);

                } else {
                    JOptionPane.showMessageDialog(this, "Error opening file: Could not open file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // геттеры
    public FileChannel getFileChannel() { return fileChannel; }
    public long getFileSize() { return fileSize; }
}