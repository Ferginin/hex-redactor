package org.example.tableSettings;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectionHandler implements TableModelListener {
    private static final Logger LOGGER = Logger.getLogger(SelectionHandler.class.getName());

    private final JTextArea textArea;
    private final JTable table;
    private final Highlighter highlighter;
    private int dataColumnsCount;

    public SelectionHandler(JTextArea textArea, JTable table){
        this.textArea = textArea;
        this.table = table;
        this.highlighter = new Highlighter(textArea);
        this.textArea.setEditable(false);

        // Initialize dataColumnsCount based on table columns
        updateDataColumnsCount();

        // Add listener to tableModel
        table.getModel().addTableModelListener(this);

        // Add mouse listener to table for highlighting
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectCells();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectCells();
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                selectCells();
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                selectCells();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                selectCells();
            }
        });
    }

    @Override
    public void tableChanged(TableModelEvent tableModelEvent) {
        updateDataColumnsCount(); // Update the column count whenever the table changes
    }

    private void updateDataColumnsCount() {
        this.dataColumnsCount = table.getColumnCount() - 3; // Adjust based on table structure
    }

    public void selectCells() {
        Set<Point> selectedCells = ((CustomTable) table).getSelectedCells();
        highlighter.clearHighlights();
        if(table.getRowCount() > 0 && table.getColumnCount() > 0) {
            for (Point cell : selectedCells) {
                int row = cell.y;
                int col = cell.x;

                // Check if the cell is not empty and contains a value
                Object value = table.getValueAt(row, col);
                if (!(value instanceof String) || ((String) value).isEmpty()) {
                    continue; // Skip empty cells
                }

                // Calculate character index
                int charIndex = (row * dataColumnsCount) + (col - 3); // Adjust for column offset

                // Check character index bounds
                if (charIndex >= 0 && charIndex < textArea.getText().length()) {
                    highlighter.highlight(charIndex);
                }
            }

            // Move caret in JTextArea
            try {
                if (!selectedCells.isEmpty()) {
                    Point firstCell = selectedCells.iterator().next();
                    int firstCharIndex = (firstCell.y * dataColumnsCount) + (firstCell.x - 3);
                    if (firstCharIndex >= 0 && firstCharIndex < textArea.getText().length()) {
                        textArea.setCaretPosition(firstCharIndex);
                        Rectangle2D rect = textArea.modelToView2D(firstCharIndex);
                        if (rect != null) {
                            textArea.scrollRectToVisible(rect.getBounds());
                        }
                    }
                }
            } catch (BadLocationException e) {
                LOGGER.log(Level.SEVERE, "Error moving caret in text area", e);
            }
        }
    }

    // Separate class for highlighting
    private record Highlighter(JTextArea textArea) {

        public void highlight(int charIndex) {
            try {
                textArea.getHighlighter().addHighlight(charIndex, charIndex + 1,
                        new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN));
            } catch (BadLocationException ex) {
                LOGGER.log(Level.SEVERE, "Error highlighting text area", ex);
            }
        }

        public void clearHighlights() {
            textArea.getHighlighter().removeAllHighlights();
        }
    }
}