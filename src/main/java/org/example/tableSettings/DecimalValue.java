package org.example.tableSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DecimalValue {
    // Обновляет отображение десятичного значения выделенного блока данных
    private final CustomTable table;
    private final JTextArea outputArea;

    public DecimalValue(CustomTable table, JTextArea outputArea) {
        this.table = table;
        this.outputArea = outputArea;

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                updateSelectionInfo();
            }
        });
    }

    public void updateSelectionInfo() {
        Set<Point> selectedCells = table.getSelectedCells();
        if (selectedCells.isEmpty()) {
            outputArea.setText("");
            return;
        }

        StringBuilder infoBuilder = new StringBuilder();
        List<Integer> byteValues = new ArrayList<>();

        // Считываем выделенные байты в порядке возрастания
        List<Point> sortedCells = new ArrayList<>(selectedCells);
        sortedCells.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));

        for (Point point : sortedCells) {
            Object value = table.getValueAt(point.y, point.x);
            if (value instanceof String && ((String) value).matches("[0-9A-Fa-f]{1,2}")) {
                try {
                    int byteValue = Integer.parseInt((String) value, 16);
                    byteValues.add(byteValue);
                } catch (NumberFormatException ignored) {
                    // Игнорируем неверные значения
                }
            }
        }

        // Обработка значений в зависимости от количества байт
        if (!byteValues.isEmpty()) {
            processByteValues(infoBuilder, byteValues);
        }

        outputArea.setText(infoBuilder.toString().trim());
    }

    private void processByteValues(StringBuilder infoBuilder, List<Integer> byteValues) {
        int size = byteValues.size();

        if (size >= 1) {
            byte int8Value = (byte) (byteValues.get(0) & 0xFF);
            int uint8Value = int8Value & 0xFF;
            infoBuilder.append(String.format("int8: %d, uint8: %d\n", int8Value, uint8Value));
        }

        if (size >= 2) {
            short int16Value = (short) ((byteValues.get(0) & 0xFF) | ((byteValues.get(1) & 0xFF) << 8));
            int uint16Value = int16Value & 0xFFFF;
            infoBuilder.append(String.format("int16: %d, uint16: %d\n", int16Value, uint16Value));
        }

        if (size >= 3) {
            int int24Value = (byteValues.get(0) & 0xFF) | ((byteValues.get(1) & 0xFF) << 8) | ((byteValues.get(2) & 0xFF) << 16);
            long uint24Value = int24Value & 0xFFFFFFL;
            infoBuilder.append(String.format("int24: %d, uint24: %d\n", int24Value, uint24Value));
        }

        if (size >= 4) {
            int int32Value = (byteValues.get(0) & 0xFF) |
                    ((byteValues.get(1) & 0xFF) << 8) |
                    ((byteValues.get(2) & 0xFF) << 16) |
                    ((byteValues.get(3) & 0xFF) << 24);
            long uint32Value = int32Value & 0xFFFFFFFFL;
            infoBuilder.append(String.format("int32: %d, uint32: %d\n", int32Value, uint32Value));

            // Float (single precision)
            float floatValue = Float.intBitsToFloat(int32Value);
            infoBuilder.append(String.format("float: %s\n", (floatValue == 0.0f) ? "NaN" : floatValue));
        }

        if (size >= 8) {
            long int64Value = (long) (byteValues.get(0) & 0xFF) |
                    ((long) (byteValues.get(1) & 0xFF) << 8) |
                    ((long) (byteValues.get(2) & 0xFF) << 16) |
                    ((long) (byteValues.get(3) & 0xFF) << 24) |
                    ((long) (byteValues.get(4) & 0xFF) << 32) |
                    ((long) (byteValues.get(5) & 0xFF) << 40) |
                    ((long) (byteValues.get(6) & 0xFF) << 48) |
                    ((long) (byteValues.get(7) & 0xFF) << 56);

            infoBuilder.append(String.format("int64: %d\n", int64Value));

            // Double (double precision)
            double doubleValue = Double.longBitsToDouble(int64Value);
            infoBuilder.append(String.format("double: %s\n", (doubleValue == 0.0) ? "NaN" : doubleValue));
        }
    }
}
