package org.example.tableFuncs;

import org.example.HexEditor;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SaveChanges extends Component {

    private final FileChannel fileChannel;

    public SaveChanges(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    // Сохраняет изменения в файл
    public void save(boolean saveAs, HexEditor hexEditor) throws IOException {
        try {
            FileChannel saveChannel = fileChannel;
            Path savePath;

            if (saveAs) {
                // Открываем диалог выбора пути сохранения
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    savePath = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
                    saveChannel = FileChannel.open(savePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    return; // Пользователь отменил сохранение
                }
            } else {
                saveChannel.truncate(0);
            }

            // Проверяем, что канал открыт
            if (saveChannel == null || !saveChannel.isOpen()) {
                JOptionPane.showMessageDialog(this, "File channel is not open", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Получаем сохраненное содержимое файла
            byte[] fileContent = hexEditor.getFileContent();

            // Записываем обновленное содержимое файла
            ByteBuffer buffer = ByteBuffer.wrap(fileContent);
            saveChannel.write(buffer);
            System.out.println("File content written to channel");

            // Обрезаем файл по текущему размеру
            saveChannel.truncate(fileContent.length);
            System.out.println("File truncated to size: " + fileContent.length);

            // Сохраняем изменения в файле
            saveChannel.force(false);
            System.out.println("File changes forced to disk");
            JOptionPane.showMessageDialog(this, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            if (saveAs) {
                // Закрываем временный канал, если был создан новый
                saveChannel.close();
                System.out.println("Temporary channel closed");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}