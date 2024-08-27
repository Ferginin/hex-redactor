package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class saveChanges extends Component {

    // Сохраняет изменения в файл
    public saveChanges(int BYTES_PER_LINE, long currentPosition, DefaultTableModel tableModel, FileChannel fileChannel, JTable hexTable) throws IOException {
        // Закрываем существующий канал
        if (fileChannel != null) {
            fileChannel.close();
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(this);
        // Открываем файл для записи
        Path file = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
        fileChannel = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        // Проходим по каждой строке таблицы
        for (int row = 0; row < hexTable.getRowCount(); row++) {
            // Получаем позицию в файле для данной строки
            long address = currentPosition + (long) row * BYTES_PER_LINE;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);
            // Создаем буфер для записи данных
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);

            // Записываем данные из таблицы в буфер
            for (int i = 0; i < BYTES_PER_LINE; i++) {
                String value = (String) tableModel.getValueAt(row, i + 2);
                if (value != null && !value.isEmpty()) {
                    try {
                        buffer.put(Byte.parseByte(value.trim(), 16)); // Проверяем, является ли значение hex числом
                    } catch (NumberFormatException e) {
                        buffer.put((byte) 0); // Записываем 0, если невозможно преобразовать
                    }
                } else {
                    buffer.put((byte) 0); // Если ячейка пуста, записываем 0
                }
            }
            buffer.flip();
            // Записываем буфер в файл
            fileChannel.write(buffer);
        }
        // Обрезаем файл по текущему размеру
        fileChannel.truncate(currentPosition + (long) hexTable.getRowCount() * BYTES_PER_LINE);
        // Сохраняем изменения в файле
        fileChannel.force(false);
        JOptionPane.showMessageDialog(this, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

}
