package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CutWorker {
    private final ByteBuffer byteBuffer;
    private final HexEditor hexEditor;

    public CutWorker(ByteBuffer byteBuffer, HexEditor hexEditor) {
        this.byteBuffer = byteBuffer;
        this.hexEditor = hexEditor;
    }

    public void cut(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || selectedCells == null) {
            return;
        }

        List<Byte> removedBytes = new ArrayList<>(); // Список для хранения удалённых байтов

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * columnCount;

        // Сортируем выделенные ячейки
        List<Point> sortedCells = new ArrayList<>(selectedCells);
        sortedCells.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));

        // Устанавливаем флаги для замены выделенных байтов на 00
        for (Point point : sortedCells) {
            int index = startAddress + (point.y * columnCount + point.x - 3); // Общий индекс
            if (index >= 0 && index < fileContent.length) {
                removedBytes.add(fileContent[index]); // Сохраняем удаляемые байты
                fileContent[index] = 0; // Заменяем на 00
            }
        }

        // Сохраняем все удалённые байты в буфер
        byteBuffer.copyBytes(removedBytes);

        // Обновляем fileContent
        hexEditor.setFileContent(fileContent);

        // Выводим удалённые байты из буфера в консоль
//        System.out.println("Обнулённые байты: " + byteBuffer.getBuffer());
    }
}