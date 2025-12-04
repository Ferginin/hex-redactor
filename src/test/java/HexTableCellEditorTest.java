import org.example.tableSettings.HexTableCellEditor;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexTableCellEditorTest {

    @Test
    public void testHexTableCellEditor() {
        // Arrange
        TableCellEditor editor = new HexTableCellEditor();
        JTable table = new JTable();
        Object value = "0A";
        boolean isSelected = true;
        int row = 0;
        int column = 0;

        // Act
        Component component = editor.getTableCellEditorComponent(table, value, isSelected, row, column);
        JTextField textField = (JTextField) component;

        // Assert
        assertEquals("0A", textField.getText());
    }
}