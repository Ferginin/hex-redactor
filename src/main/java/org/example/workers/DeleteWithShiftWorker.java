package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DeleteWithShiftWorker {
    private final HexEditor hexEditor;

    public DeleteWithShiftWorker(HexEditor hexEditor) {
        this.hexEditor = hexEditor;
    }

    public void removeBytes(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || selectedCells == null) {
            return;
        }

        List<Byte> removedBytes = new ArrayList<>(); // Список для хранения удаленных байтов

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * columnCount;
        int deletePosition = -1;

        // Сортируем выделенные ячейки
        List<Point> sortedCells = new ArrayList<>(selectedCells);
        sortedCells.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));

        // Устанавливаем флаги для удаления выделенных байтов
        for (Point point : sortedCells) {
            int index = startAddress + (point.y * columnCount + point.x - 3); // Общий индекс
            if (index >= 0 && index < fileContent.length) {
                removedBytes.add(fileContent[index]); // Сохраняем удаленные байты
                deletePosition = Math.max(deletePosition, index); // Запоминаем максимальный индекс
            }
        }

        // Проверяем, было ли найдено место для вставки
        if (deletePosition == -1) {
            throw new IllegalArgumentException("Не удалось определить место вставки.");
        }

        // Создаем новый массив для сохранения измененного содержимого
        byte[] newContent = new byte[fileContent.length - removedBytes.size()];

        // Копируем байты до места вставки
        System.arraycopy(fileContent, 0, newContent, 0, deletePosition - removedBytes.size() + 1);

        // Копируем оставшиеся байты
        System.arraycopy(fileContent, deletePosition + 1, newContent, deletePosition - removedBytes.size() + 1, fileContent.length - deletePosition - 1);

        // Обновляем fileContent
        hexEditor.setFileContent(newContent);
        hexEditor.setFileSize(newContent.length);
    }
}