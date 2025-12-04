package org.example.tableFuncs;

import org.example.HexEditor;
import javax.swing.table.DefaultTableModel;

public class FileContentUpdater {
    private final HexEditor hexEditor;

    public FileContentUpdater(HexEditor hexEditor) {
        this.hexEditor = hexEditor;
    }

    public void updateFileContent(DefaultTableModel tableModel, int BYTES_PER_LINE, int currentPage, int pageSize) {
        byte[] fileContent = hexEditor.getFileContent();
        int startPosition = currentPage * pageSize * BYTES_PER_LINE; // стартовая позиция для записи в fileContent
        for (int row = 0; row < tableModel.getRowCount(); row++) { // по таблице идем сначала
            for (int col = 3; col < tableModel.getColumnCount(); col++) { // начиная с 4 столбца
                long bytePosition = startPosition + ((long) row * BYTES_PER_LINE) + col - 3; // меняем позицию, соответствуя смещению в таблице
                String value = (String) tableModel.getValueAt(row, col); // получаем нужный байт в таблице
                if (value != null && !value.isEmpty()) {
                    try {
                        byte byteValue = (byte) Integer.parseInt(value.trim(), 16);
                        fileContent[(int) bytePosition] = byteValue;
                    } catch (NumberFormatException e) {
                        // Записываем 0, если невозможно преобразовать
                        fileContent[(int) bytePosition] = 0;
                    }
                } else {
                    // Если ячейка пуста, записываем 0
                    if (((int) bytePosition + col - 3) < fileContent.length) {
                        fileContent[(int) bytePosition] = 0;
                    }
                }
            }
        }

        hexEditor.setFileContent(fileContent);
    }
}