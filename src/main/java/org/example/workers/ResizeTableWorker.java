package org.example.workers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ResizeTableWorker extends SwingWorker<Void, Void> {
    private final DefaultTableModel tableModel;
    private final int BYTES_PER_LINE;

    public ResizeTableWorker(DefaultTableModel tableModel, int BYTES_PER_LINE) {
        this.tableModel = tableModel;
        this.BYTES_PER_LINE = BYTES_PER_LINE;
    }

    @Override
    protected Void doInBackground(){
        // Обновляем модель таблицы
        tableModel.setColumnCount(BYTES_PER_LINE + 3);
        String[] columnNames = new String[BYTES_PER_LINE + 3]; // +3 для нумерации строк и заголовка
        columnNames[0] = "No";   // Нумерация строк
        columnNames[1] = "Address"; // Адрес
        columnNames[2] = "Hex"; // Заголовок для hex данных
        for (int i = 0; i < BYTES_PER_LINE; i++) {
            columnNames[i + 3] = String.valueOf(i + 1); // Нумерация столбцов (1, 2, 3 ...)
        }
        tableModel.setColumnIdentifiers(columnNames);

        return null;
    }

    @Override
    protected void done() {
        try {
            get(); // Wait for the background task to complete
        } catch (Exception e) {
            System.err.println("Error resizing table: " + e.getMessage());
        }
    }
}