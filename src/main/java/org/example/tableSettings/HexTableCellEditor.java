package org.example.tableSettings;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.EventObject;

public class HexTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField textField;

    public HexTableCellEditor() {
        textField = new JTextField();
        textField.setHorizontalAlignment(JTextField.LEFT);
        AbstractDocument document = new PlainDocument();
        document.setDocumentFilter(new HexDocumentFilter());
        textField.setDocument(document);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText((String) value);
        return textField;
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        String value = textField.getText();
        if (!isValidHex(value)) {
            JOptionPane.showMessageDialog(textField, "Invalid hex value", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return super.stopCellEditing();
    }

    private boolean isValidHex(String value) {
        if (value.length() != 2) {
            return false;
        }
        try {
            Integer.parseInt(value, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}