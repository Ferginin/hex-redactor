package org.example.tableFuncs;

import org.example.tableSettings.CustomTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchBytes {
    private static final Logger LOGGER = Logger.getLogger(SearchBytes.class.getName());

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JRadioButton exactMatchButton;
    private final JRadioButton maskMatchButton;
    private final JTextArea textArea;

    public SearchBytes(JTable table, JTextField searchField, JRadioButton exactMatchButton,
                       JRadioButton maskMatchButton, JTextArea textArea) {
        this.table = table;
        this.tableModel = (DefaultTableModel) table.getModel();
        this.searchField = searchField;
        this.exactMatchButton = exactMatchButton;
        this.maskMatchButton = maskMatchButton;
        this.textArea = textArea;
    }

    public void Search() {
        String input = searchField.getText().trim();
        List<Point> foundCells = new ArrayList<>();

        if (exactMatchButton.isSelected()) {
            foundCells = ExactSearch(input);
        } else if (maskMatchButton.isSelected()) {
            foundCells = MaskSearch(input);
        }

        highlightFoundCells(foundCells); // Выделение текста в JTable
        highlightTextArea(foundCells); // Выделение текста в JTextArea
    }

    private List<Point> ExactSearch(String input) {
        List<Point> foundCells = new ArrayList<>();
        String[] exactParts = input.split(" "); // Разделяем ввод на части

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 3; col <= tableModel.getColumnCount() - exactParts.length; col++) { // Учитываем длину массива
                boolean matches = true;

                for (int i = 0; i < exactParts.length; i++) {
                    Object value = tableModel.getValueAt(row, col + i);
                    if (value instanceof String cellValue) {
                        if (!cellValue.equals(exactParts[i])) {
                            matches = false;
                            break;
                        }
                    } else {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    for (int i = 0; i < exactParts.length; i++) {
                        foundCells.add(new Point(col + i, row)); // Добавляем все найденные ячейки
                    }
                }
            }
        }

        return foundCells;
    }

    private List<Point> MaskSearch(String input) {
        List<Point> foundCells = new ArrayList<>();
        String[] maskParts = input.split(" ");

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 3; col <= tableModel.getColumnCount() - maskParts.length; col++) { // Учитываем длину маски
                boolean matches = true;

                for (int i = 0; i < maskParts.length; i++) {
                    Object value = tableModel.getValueAt(row, col + i);
                    if (value instanceof String cellValue) {
                        String maskPart = maskParts[i];
                        if (!matchesMask(cellValue, maskPart)) {
                            matches = false;
                            break;
                        }
                    } else {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    for (int i = 0; i < maskParts.length; i++) {
                        foundCells.add(new Point(col + i, row)); // Добавляем все найденные ячейки
                    }
                }
            }
        }

        return foundCells;
    }

    private boolean matchesMask(String cellValue, String maskPart) {
        return maskPart.equals("?") || cellValue.equals(maskPart);
    }

    private void highlightFoundCells(List<Point> foundCells) {
        ((CustomTable) table).getSelectedCells().clear();

        for (Point cell : foundCells) {
            ((CustomTable) table).getSelectedCells().add(cell);
        }

        table.repaint();
    }

    private void highlightTextArea(List<Point> foundCells) {
        // Очистка выделения в текстовой области
        textArea.getHighlighter().removeAllHighlights();

        for (Point cell : foundCells) {
            int row = cell.y;
            int col = cell.x;

            // Проверяем, что ячейка содержит значение
            Object value = table.getValueAt(row, col);
            if (value instanceof String && !((String) value).isEmpty()) {
                // Получаем текстовый эквивалент
                int charIndex = (row * (tableModel.getColumnCount() - 3)) + (col - 3); // Рассчитываем индекс символа

                // Выделяем соответствующий символ в JTextArea
                try {
                    textArea.getHighlighter().addHighlight(charIndex, charIndex + 1,
                            new DefaultHighlighter.DefaultHighlightPainter(Color.RED));
                } catch (BadLocationException e) {
                    LOGGER.log(Level.SEVERE, "Error highlighting text area", e);
                }
            }
        }
    }
}