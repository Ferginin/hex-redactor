package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class PasteWithShiftWorker {
    private final ByteBuffer byteBuffer;
    private final HexEditor hexEditor;

    public PasteWithShiftWorker(ByteBuffer byteBuffer, HexEditor hexEditor) {
        this.byteBuffer = byteBuffer;
        this.hexEditor = hexEditor;
    }

    public void insertBytes(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || byteBuffer.isEmpty()) {
            return;
        }

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * columnCount;
        int insertPosition = -1;

        // Находим последнюю выделенную ячейку
        for (Point point : selectedCells) {
            int index = startAddress + (point.y * columnCount + point.x - 3); // Общий индекс
            if (index >= 0 && index < fileContent.length) {
                insertPosition = Math.max(insertPosition, index); // Запоминаем максимальный индекс
            }
        }

        // Проверяем, было ли найдено место для вставки
        if (insertPosition == -1) {
            throw new IllegalArgumentException("Не удалось определить место вставки.");
        }

        // Получаем скопированные байты из буфера
        List<Byte> copiedBytes = byteBuffer.getBuffer();

        // Создаем новый массив для сохранения измененного содержимого
        byte[] newContent = new byte[fileContent.length + copiedBytes.size()];

        // Копируем байты до места вставки
        System.arraycopy(fileContent, 0, newContent, 0, insertPosition);

        // Вставляем скопированные байты
        for (int i = 0; i < copiedBytes.size(); i++) {
            newContent[insertPosition + i] = copiedBytes.get(i);
        }

        // Копируем оставшиеся байты
        System.arraycopy(fileContent, insertPosition, newContent, insertPosition + copiedBytes.size(), fileContent.length - insertPosition);

        // Обновляем fileContent
        hexEditor.setFileContent(newContent);
        hexEditor.setFileSize(newContent.length);

        // Выводим вставленные байты в консоль
//        System.out.println("Вставленные байты: " + copiedBytes);
    }
}