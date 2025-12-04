import org.example.tableSettings.CustomTable;
import org.example.tableSettings.DecimalValue;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalValueTest {

    @Test
    public void testDecimalValue() {
        // Arrange
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"No", "Address", "Hex", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"}, 0);
        tableModel.addRow(new Object[]{"1", "00000000", "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 ", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10"});
        CustomTable table = new CustomTable(tableModel);
        JTextArea outputArea = new JTextArea();

        DecimalValue decimalValue = new DecimalValue(table, outputArea);

        // Act
        table.getSelectedCells().add(new Point(3, 0));
        table.getSelectedCells().add(new Point(4, 0));
        table.getSelectedCells().add(new Point(5, 0));
        decimalValue.updateSelectionInfo();

        // Assert
        assertEquals("int8: 1, uint8: 1\nint16: 513, uint16: 513\nint24: 197121, uint24: 197121", outputArea.getText());
    }
}