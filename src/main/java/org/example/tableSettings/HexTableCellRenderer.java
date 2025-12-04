package org.example.tableSettings;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class HexTableCellRenderer extends JLabel implements TableCellRenderer {

    // Цвет выделения
    private final Color selectionColor = new Color(200, 200, 255);

    public HexTableCellRenderer() {
        setOpaque(true); // Убедитесь, что ячейка непрозрачна
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value != null ? value.toString() : "");

        if (isSelected) {
            setBackground(selectionColor);
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }
}