import org.example.HexEditor;
import org.example.tableFuncs.SaveChanges;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SaveChangesTest {

    @Test
    public void testSaveChanges() throws IOException {
        // Arrange
        Path tempFile = Files.createTempFile("test", ".bin");
        FileChannel fileChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE, StandardOpenOption.READ);
        HexEditor hexEditor = new HexEditor();
        hexEditor.setFileContent(new byte[]{0x01, 0x02, 0x03, 0x04});

        SaveChanges saveChanges = new SaveChanges(fileChannel);

        // Act
        assertDoesNotThrow(() -> saveChanges.save(false, hexEditor));

        // Assert
        byte[] savedContent = Files.readAllBytes(tempFile);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, savedContent);

        // Cleanup
        Files.delete(tempFile);
    }
}