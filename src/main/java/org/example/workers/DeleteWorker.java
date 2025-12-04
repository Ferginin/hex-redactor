package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.Set;

public class DeleteWorker {
    private final HexEditor hexEditor;

    public DeleteWorker(HexEditor hexEditor) {
        this.hexEditor = hexEditor;
    }

    public void removeBytesWithPadding(Set<Point> selectedCells, int columnCount, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || selectedCells == null) {
            return;
        }

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * columnCount;

        // Устанавливаем флаги для замены выделенных байтов на 00
        for (Point point : selectedCells) {
            int index = startAddress + (point.y * columnCount + point.x - 3); // Общий индекс
            if (index >= 0 && index < fileContent.length) {
                fileContent[index] = 0; // Заменяем на 00
            }
        }

        // Обновляем fileContent
        hexEditor.setFileContent(fileContent);
    }
}