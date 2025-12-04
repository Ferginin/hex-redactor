import org.example.HexEditor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class HexEditorTest {

    @Test
    public void testHexEditorInitialization() {
        // Arrange
        HexEditor hexEditor = new HexEditor();

        // Act
        byte[] fileContent = hexEditor.getFileContent();

        // Assert
        assertNull(fileContent);
    }
}