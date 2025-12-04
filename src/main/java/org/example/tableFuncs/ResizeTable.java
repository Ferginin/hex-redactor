package org.example.tableFuncs;

import org.example.workers.ResizeTableWorker;
import javax.swing.table.DefaultTableModel;

public class ResizeTable {
    // Метод для изменения размеров таблицы
    public void resize(DefaultTableModel tableModel, int BYTES_PER_LINE) {
        // Очищаем модель таблицы перед изменением размера
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        // Убеждаемся, что изменение размера таблицы выполняется в EDT
        new ResizeTableWorker(tableModel, BYTES_PER_LINE).execute();
    }
}