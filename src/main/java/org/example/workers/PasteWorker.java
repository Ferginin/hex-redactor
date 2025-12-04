package org.example.workers;

import org.example.HexEditor;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class PasteWorker {
    private final ByteBuffer byteBuffer;
    private final HexEditor hexEditor;

    public PasteWorker(ByteBuffer byteBuffer, HexEditor hexEditor) {
        this.byteBuffer = byteBuffer;
        this.hexEditor = hexEditor;
    }

    public void replaceBytes(Set<Point> selectedCells, int BYTES_PER_LINE, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        if (fileContent == null || byteBuffer.isEmpty()) {
            return;
        }

        // Вычисляем начальный адрес для текущей страницы
        int startAddress = (currentPage) * pageSize * BYTES_PER_LINE;

        // Получаем байты из буфера
        List<Byte> bytesToReplace = byteBuffer.getBuffer();
        int bufferSize = bytesToReplace.size();

        // Находим первую выделенную ячейку
        int firstIndex = -1;
        for (Point point : selectedCells) {
            firstIndex = startAddress + (point.y * BYTES_PER_LINE + point.x - 3); // Общий индекс
            break; // Выходим из цикла после первой найденной ячейки
        }

        // Проверяем, было ли найдено место для замены
        if (firstIndex == -1 || firstIndex >= fileContent.length) {
            throw new IllegalArgumentException("Не удалось определить место замены.");
        }

        // Заменяем байты, начиная с первой выделенной ячейки
        for (int i = 0; i < bufferSize; i++) {
            int indexToReplace = firstIndex + i;
            if (indexToReplace < fileContent.length) {
                fileContent[indexToReplace] = bytesToReplace.get(i); // Заменяем байт
            } else {
                break; // Если вышли за пределы массива, останавливаем замену
            }
        }

        // Обновляем fileContent
        hexEditor.setFileContent(fileContent);

        // Выводим заменённые байты в консоль
//        System.out.println("Заменённые байты: " + bytesToReplace);
    }
}