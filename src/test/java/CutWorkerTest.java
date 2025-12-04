import org.example.HexEditor;
import org.example.workers.ByteBuffer;
import org.example.workers.CutWorker;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CutWorkerTest {

    @Test
    public void testCutWorker() {
        // Arrange
        ByteBuffer byteBuffer = new ByteBuffer();
        HexEditor hexEditor = new HexEditor();
        hexEditor.setFileContent(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10});

        Set<Point> selectedCells = new HashSet<>();
        selectedCells.add(new Point(3, 0));
        selectedCells.add(new Point(4, 0));
        selectedCells.add(new Point(5, 0));

        int columnCount = 16;
        int currentPage = 0;
        int pageSize = 1;

        CutWorker cutWorker = new CutWorker(byteBuffer, hexEditor);

        // Act
        cutWorker.cut(selectedCells, columnCount, currentPage, pageSize);

        // Assert
        assertEquals(3, byteBuffer.getBuffer().size());
        assertEquals((byte) 0x01, byteBuffer.getBuffer().get(0));
        assertEquals((byte) 0x02, byteBuffer.getBuffer().get(1));
        assertEquals((byte) 0x03, byteBuffer.getBuffer().get(2));

        byte[] expectedContent = new byte[]{0x00, 0x00, 0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        assertArrayEquals(expectedContent, hexEditor.getFileContent());
    }
}