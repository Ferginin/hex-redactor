package org.example.tableFuncs;

import org.example.HexEditor;
import org.example.tableSettings.TextAreaHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.nio.ByteBuffer;

public class FileLoader extends SwingWorker<Void, Void> {
    private final int BYTES_PER_LINE;
    private final DefaultTableModel tableModel;
    private final JTextArea textArea;
    private final long fileSize;
    private final int currentPage;
    private final int pageSize;
    private final HexEditor hexEditor;

    public FileLoader(int BYTES_PER_LINE, DefaultTableModel tableModel, JTextArea textArea, long fileSize, int currentPage, int pageSize, HexEditor hexEditor) {
        this.BYTES_PER_LINE = BYTES_PER_LINE;
        this.tableModel = tableModel;
        this.textArea = textArea;
        this.fileSize = fileSize;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hexEditor = hexEditor;
    }

    @Override
    protected Void doInBackground(){
        long startPosition = (long) currentPage * pageSize * BYTES_PER_LINE;
        long endPosition = Math.min(startPosition + (long) pageSize * BYTES_PER_LINE, fileSize);

        byte[] fileContent = hexEditor.getFileContent();

        tableModel.setRowCount(0); // Очищаем таблицу
        if (textArea != null) {
            textArea.setText(""); // Очищаем текстовую область
        }

        AddRow addRow = new AddRow(currentPage);
        for (long currentPosition = startPosition; currentPosition < endPosition; currentPosition += BYTES_PER_LINE) {
            int bytesToRead = (int) Math.min(BYTES_PER_LINE, fileSize - currentPosition);
            ByteBuffer buffer = ByteBuffer.wrap(fileContent, (int) currentPosition, bytesToRead);
            addRow.add(buffer, BYTES_PER_LINE, currentPosition, tableModel);
        }

        return null;
    }

    @Override
    protected void done() {
        if (textArea != null) {
            TextAreaHandler.updateTextArea(tableModel, textArea);
        } else {
            System.err.println("TextArea is null");
        }
    }
}