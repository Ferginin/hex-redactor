package org.example.tableSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

public class CustomTable extends JTable {
    private int startRow = -1;
    private int startCol = -1;
    private int endRow = -1;
    private int endCol = -1;
    private final Set<Point> selectedCells = new HashSet<>();
    private final DefaultTableModel tableModel;
    private int selectionMode = 0; // 0 - стандарт, 1 - 2 байта, 2 - 4 байта, 3 - 8 байт

    public CustomTable(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        setModel(tableModel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && row < getRowCount() && col >= 0 && col < getColumnCount()) {
                    if (e.getClickCount() == 1) {
                        NumBytes(row, col); // Выделяем в зависимости от режима
                    }
                    if (e.getClickCount() == 2) {
                        if (tableModel.getValueAt(row, col) != null && col >= 3) {
                            editCellAt(row, col, e); // Переводим ячейку в режим редактирования при двойном нажатии
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());

                // Prevent selection if the column is less than 3
                if (tableModel.getValueAt(row, col) != null && col >= 3) {
                    startRow = row;
                    startCol = col;
                    endRow = row;
                    endCol = col;
                    selectedCells.clear();

                    if (selectionMode == 0) {
                        selectedCells.add(new Point(startCol, startRow)); // Выделяем только одну ячейку
                    } else {
                        NumBytes(row, col); // Выделяем в зависимости от режима
                    }
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                try {
                    if (tableModel.getValueAt(row, col) != null && col >= 3) {
                        endRow = row;
                        endCol = col;
                        selectedCells.add(new Point(endCol, endRow));
                        repaint();
                    }
                } catch (Exception e1) {
                    selectedCells.clear();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());

                if (selectionMode == 0 && col >= 3) { // Ensure column is valid
                    if (row >= 0 && row < getRowCount() &&
                            tableModel.getValueAt(row, col) != null) {
                        selectedCells.clear();
                        if (startRow == row) {
                            fillRowSelection(startRow, startCol, col);
                        } else {
                            // Handle reverse selection
                            if (row < startRow) {
                                fillStandardSelection(row, col, startRow, startCol);
                            } else {
                                fillStandardSelection(startRow, startCol, row, col);
                            }
                        }
                        repaint();
                    }
                }
            }
        });
    }

    private void NumBytes(int row, int col) {
        int count = switch (selectionMode) {// 2 байта
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            default -> 1;
        };

        int currentCol = col;
        int currentRow = row;
        // Always add the first selected cell
        selectedCells.add(new Point(currentCol, currentRow));
        count--;

        while (count > 0) {
            if (currentCol < getColumnCount() - 1) {
                currentCol++;
                selectedCells.add(new Point(currentCol, currentRow));
            } else {
                // Move to the next row
                currentCol = 3; // Start from column 3
                currentRow++;
                if (currentRow < getRowCount() && currentCol < getColumnCount()) {
                    selectedCells.add(new Point(currentCol, currentRow));
                } else {
                    break; // Exit loop if out of bounds
                }
            }
            count--;
        }
    }

    private void fillRowSelection(int row, int startCol, int endCol) {
        int minCol = Math.min(startCol, endCol);
        int maxCol = Math.max(startCol, endCol);

        // Select cells only in one row
        for (int c = minCol; c <= maxCol; c++) {
            if (tableModel.getValueAt(row, c) != null && c >= 3) {
                selectedCells.add(new Point(c, row));
            }
        }
    }

    private void fillStandardSelection(int startRow, int startCol, int endRow, int endCol) {
        int minRow = Math.min(startRow, endRow);
        int maxRow = Math.max(startRow, endRow);

        selectedCells.clear(); // Clear before new selection

        // Select all cells in range
        for (int r = minRow; r <= maxRow; r++) {
            if (r == startRow) {
                for (int c = startCol; c < getColumnCount(); c++) {
                    if (tableModel.getValueAt(r, c) != null && c >= 3) {
                        selectedCells.add(new Point(c, r));
                    }
                }
            } else if (r == maxRow) {
                for (int c = 3; c <= endCol; c++) {
                    if (tableModel.getValueAt(r, c) != null && c >= 3) {
                        selectedCells.add(new Point(c, r));
                    }
                }
            } else {
                for (int c = 3; c < getColumnCount(); c++) {
                    if (tableModel.getValueAt(r, c) != null) {
                        selectedCells.add(new Point(c, r));
                    }
                }
            }
        }
    }

    public void setSelectionMode(int mode) {
        this.selectionMode = mode;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        //System.out.println("Row: " + row + ", Column: " + column);
        if (row >= 0 && row <= getRowCount() && column >= 0 && column <= getColumnCount()) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (selectedCells.contains(new Point(column, row))) {
                c.setBackground(getSelectionBackground());
                c.setForeground(getSelectionForeground());
            } else {
                c.setBackground(getBackground());
                c.setForeground(getForeground());
            }
            return c;
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column >= 3; // Cells in first three columns are not editable
    }

    @Override
    public boolean getCellSelectionEnabled() {
        return true;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return getDefaultRenderer(getColumnClass(column));
    }

    public Set<Point> getSelectedCells() {
        return selectedCells;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof MouseEvent && ((MouseEvent) e).getClickCount() < 2) {
            return false; // Предотвращаем переход в режим редактирования при одиночном нажатии
        }
        return super.editCellAt(row, column, e);
    }
}