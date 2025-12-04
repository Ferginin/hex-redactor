package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CopyWorker {
    private final ByteBuffer byteBuffer;
    private final HexEditor hexEditor;

    public CopyWorker(ByteBuffer byteBuffer, HexEditor hexEditor) {
        this.byteBuffer = byteBuffer;
        this.hexEditor = hexEditor;
    }

    public void copy(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || selectedCells == null) {
            return;
        }

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * columnCount;

        List<Byte> copiedBytes = new ArrayList<>();

        // Сортируем выделенные ячейки
        List<Point> sortedCells = new ArrayList<>(selectedCells);
        sortedCells.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));

        // Копируем выделенные байты в порядке возрастания
        for (Point point : sortedCells) {
            int index = startAddress + (point.y * columnCount + point.x - 3); // Общий индекс
            if (index >= 0 && index < fileContent.length) {
                copiedBytes.add(fileContent[index]);
            }
        }

        // Сохраняем в общий буфер
        byteBuffer.copyBytes(copiedBytes);
//        System.out.println("Скопированные байты: " + copiedBytes);
    }
}