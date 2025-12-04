import org.example.tableFuncs.AddRow;
import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddRowTest {

    @Test
    public void testAddRow() {
        // Arrange
        int currentPage = 0;
        int BYTES_PER_LINE = 16;
        long currentPosition = 0;
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"No", "Address", "Hex", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"}, 0);
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        ByteBuffer buffer = ByteBuffer.wrap(data);

        AddRow addRow = new AddRow(currentPage);

        // Act
        addRow.add(buffer, BYTES_PER_LINE, currentPosition, tableModel);

        // Assert
        assertEquals(1, tableModel.getRowCount());
        assertEquals("1", tableModel.getValueAt(0, 0));
        assertEquals("00000000", tableModel.getValueAt(0, 1));
        assertEquals("01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 ", tableModel.getValueAt(0, 2));
        assertEquals("01", tableModel.getValueAt(0, 3));
        assertEquals("10", tableModel.getValueAt(0, 18));
    }
}