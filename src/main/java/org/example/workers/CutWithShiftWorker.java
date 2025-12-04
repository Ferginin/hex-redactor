package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CutWithShiftWorker {
    private final ByteBuffer byteBuffer;
    private final HexEditor hexEditor;

    public CutWithShiftWorker(ByteBuffer byteBuffer, HexEditor hexEditor) {
        this.byteBuffer = byteBuffer;
        this.hexEditor = hexEditor;
    }

    public void cutBytes(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || selectedCells == null) {
            return;
        }

        List<Byte> removedBytes = new ArrayList<>(); // Список для хранения вырезанных байтов

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
                removedBytes.add(fileContent[index]); // Сохраняем вырезанные байты
                deletePosition = Math.max(deletePosition, index); // Запоминаем максимальный индекс
            }
        }

        // Сохраняем все удалённые байты в буфер
        byteBuffer.copyBytes(removedBytes);

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

        // Выводим удаленные байты из буфера в консоль
//        System.out.println("Вырезанные байты: " + removedBytes);
    }
}