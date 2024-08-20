package org.example;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class HexEditor extends JFrame {

    private static final int BYTES_PER_LINE = 16;
    private final JTable hexTable;
    private final DefaultTableModel tableModel;
    private FileChannel fileChannel;
    private long fileSize;
    private long currentPosition;

    public HexEditor() {
        super("Hex Redactor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Создаем меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        fileMenu.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveChanges();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(HexEditor.this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(saveItem);
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Создаем модель таблицы
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2; // Разрешаем редактирование только колонок данных
            }
        };
        String[] columnNames = new String[BYTES_PER_LINE + 2];
        Arrays.fill(columnNames, 2, BYTES_PER_LINE + 2, ""); // Заполняем названия колонок
        columnNames[0] = "Address";
        columnNames[1] = "Hex";
        tableModel.setColumnIdentifiers(columnNames);

        // Создаем таблицу
        hexTable = new JTable(tableModel);
        hexTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = hexTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Обновляем адрес в строке таблицы
                        tableModel.setValueAt(String.format("%08X", currentPosition + (long) selectedRow * BYTES_PER_LINE), selectedRow, 0);
                        // Обновляем данные в строке таблицы
                        updateRow(selectedRow);
                    }
                }
            }
        });
        hexTable.getColumnModel().getColumn(0).setPreferredWidth(80); // Ширина колонки адреса
        hexTable.getColumnModel().getColumn(1).setPreferredWidth(BYTES_PER_LINE * 3 + 10); // Ширина колонки hex

        // Добавляем таблицу в основное окно
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(hexTable), BorderLayout.CENTER);

        setVisible(true);
    }

    // Открывает файл с помощью диалогового окна
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Закрываем предыдущий файл, если он открыт
                if (fileChannel != null) {
                    fileChannel.close();
                }
                // Открываем выбранный файл
                Path file = fileChooser.getSelectedFile().toPath();
                fileChannel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
                fileSize = fileChannel.size();
                currentPosition = 0;

                // Очищаем таблицу
                tableModel.setRowCount(0);

                // Заполняем таблицу данными
                updateTable();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Обновляет таблицу данными из файла
    private void updateTable() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            while (currentPosition < fileSize) {
                int bytesRead = fileChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                buffer.flip();
                addRow(buffer);
                buffer.clear();
                currentPosition += bytesRead;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Добавляет строку данных в таблицу
    private void addRow(ByteBuffer buffer) {
        String[] rowData = new String[BYTES_PER_LINE + 2];
        rowData[0] = String.format("%08X", currentPosition);
        StringBuilder hexString = new StringBuilder();
        StringBuilder asciiString = new StringBuilder();
        int bytesRead = buffer.remaining();
        for (int i = 0; i < bytesRead; i++) {
            byte b = buffer.get();
            rowData[i + 2] = String.format("%02X", b);
            hexString.append(String.format("%02X ", b));
            if (b >= 32 && b <= 126) {
                asciiString.append((char) b);
            } else {
                asciiString.append(".");
            }
            if (i % 4 == 3) {
                hexString.append(" ");
            }
        }
        rowData[1] = hexString.toString();
        tableModel.addRow(rowData);
    }

    // Обновляет строку таблицы по выбранному индексу
    private void updateRow(int row) {
        try {
            // Получаем позицию в файле для выбранной строки
            long address = currentPosition + (long) row * BYTES_PER_LINE;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            // Читаем данные из файла
            fileChannel.read(buffer);
            buffer.flip();
            // Заполняем данные в строке таблицы
            for (int i = 0; i < BYTES_PER_LINE; i++) {
                if (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    tableModel.setValueAt(String.format("%02X", b), row, i + 2);
                } else {
                    tableModel.setValueAt("", row, i + 2);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Сохраняет изменения в файл
    private void saveChanges() throws IOException {
        // Проходим по каждой строке таблицы
        for (int row = 0; row < hexTable.getRowCount(); row++) {
            // Получаем позицию в файле для данной строки
            long address = currentPosition + (long) row * BYTES_PER_LINE;
            // Перемещаем позицию курсора в файле
            fileChannel.position(address);
            // Создаем буфер для записи данных
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            // Записываем данные из таблицы в буфер
            for (int i = 0; i < BYTES_PER_LINE; i++) {
                String value = (String) tableModel.getValueAt(row, i + 2);
                if (!value.isEmpty()) {
                    buffer.put(Byte.parseByte(value, 16));
                }
            }
            buffer.flip();
            // Записываем буфер в файл
            fileChannel.write(buffer);
        }
        // Обновляем размер файла
        fileChannel.truncate(currentPosition + (long) hexTable.getRowCount() * BYTES_PER_LINE);
        // Сохраняем изменения в файле
        fileChannel.force(false);
        JOptionPane.showMessageDialog(this, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HexEditor());
    }
}