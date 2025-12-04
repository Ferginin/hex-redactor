import org.example.HexEditor;
import org.example.tableFuncs.FileContentUpdater;
import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class FileContentUpdaterTest {

    @Test
    public void testUpdateFileContent() {
        // Arrange
        HexEditor hexEditor = new HexEditor();
        hexEditor.setFileContent(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"No", "Address", "Hex", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"}, 0);
        tableModel.addRow(new Object[]{"1", "00000000", "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10"});

        FileContentUpdater updater = new FileContentUpdater(hexEditor);
        int BYTES_PER_LINE = 16;
        int currentPage = 0;
        int pageSize = 1;

        // Act
        updater.updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);

        // Assert
        byte[] expectedContent = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        assertArrayEquals(expectedContent, hexEditor.getFileContent());
    }
}