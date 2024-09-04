package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class HexEditor extends JFrame {

    private int BYTES_PER_LINE = 16;
    private int rows = 0; // Число строк
    private static final String[] DATA_SIZES = {"1 byte", "2 bytes", "4 bytes", "8 bytes"};
    private JTable hexTable;
    private DefaultTableModel tableModel;
    private FileChannel fileChannel;
    private long fileSize;
    private long currentPosition;
    private int selectedRow = -1;
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private JLabel decimalValueLabel;
    private JComboBox<String> dataSizeComboBox;
    private JTextField searchField;
    private JRadioButton exactMatchButton;
    private JRadioButton maskMatchButton;
    private JButton searchButton;

    public HexEditor() {
        super("Hex Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Создаем разделитель
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1, 10));

        // Создаем меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> new openFile(tableModel, fileChannel, fileSize, currentPosition, textArea));
        fileMenu.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            try {
                new saveChanges(BYTES_PER_LINE, currentPosition, tableModel, fileChannel, hexTable);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(HexEditor.this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        fileMenu.add(saveItem);
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> dispose());
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Создаем элементы управления для изменения размеров таблицы
        JLabel rowsLabel = new JLabel("Rows: ");
        JTextField rowsField = new JTextField("0");
        JLabel colsLabel = new JLabel("Cols: ");
        JTextField colsField = new JTextField("16");

        // Кнопка для изменения размеров
        JButton resizeButton = new JButton("Resize");
        resizeButton.addActionListener(e -> {
            try {
                rows = Integer.parseInt(rowsField.getText());
                BYTES_PER_LINE = Integer.parseInt(colsField.getText());
                resizeTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(HexEditor.this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Создаем элементы управления для поиска
        JLabel searchLabel = new JLabel("Search: ");
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        exactMatchButton = new JRadioButton("Exact Match");
        maskMatchButton = new JRadioButton("Mask Match");
        ButtonGroup searchGroup = new ButtonGroup();
        searchGroup.add(exactMatchButton);
        searchGroup.add(maskMatchButton);
        exactMatchButton.setSelected(true);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search(fileChannel);
            }
        });

        // Добавляем элементы управления для поиска на панель
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(exactMatchButton);
        searchPanel.add(maskMatchButton);
        searchPanel.add(searchButton);

        // Добавляем элементы управления на панель
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(rowsLabel);
        controlPanel.add(rowsField);
        controlPanel.add(colsLabel);
        controlPanel.add(colsField);
        controlPanel.add(resizeButton);
        // Добавляем searchPanel в controlPanel
        controlPanel.add(searchPanel);

        // Создаем модель таблицы
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3; // Разрешаем редактирование только колонок данных
            }
        };

        String[] columnNames = new String[BYTES_PER_LINE + 3]; // +3 для нумерации строк и заголовка
        columnNames[0] = "No";   // Нумерация строк
        columnNames[1] = "Address"; // Адрес
        columnNames[2] = "Hex"; // Заголовок для hex данных
        for (int i = 0; i < BYTES_PER_LINE; i++) {
            columnNames[i + 3] = String.valueOf(i + 1); // Нумерация столбцов (1, 2, 3 ...)
        }
        tableModel.setColumnIdentifiers(columnNames);

        // Создаем выпадающий список для выбора размера данных
        dataSizeComboBox = new JComboBox<>(DATA_SIZES);
        dataSizeComboBox.setSelectedIndex(0); // По умолчанию 1 байт
        dataSizeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow != -1) {
                    new updateDecimalValue(selectedRow, hexTable.getSelectedColumn(), dataSizeComboBox, tableModel, decimalValueLabel);
                }
            }
        });

        // Создаем JLabel для отображения значения байта
        decimalValueLabel = new JLabel("Decimal Value: ");

        // Создаем таблицу
        hexTable = new JTable(tableModel) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };

        hexTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = hexTable.getSelectedRow();
                int selectedColumn = hexTable.getSelectedColumn();
                new updateDecimalValue(selectedRow, selectedColumn, dataSizeComboBox, tableModel, decimalValueLabel);
            }
        });

        hexTable.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedRow >= 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    new saveByte(selectedRow, hexTable.getSelectedColumn(), BYTES_PER_LINE, currentPosition, tableModel, fileChannel, hexTable, decimalValueLabel, dataSizeComboBox);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        hexTable.getColumnModel().getColumn(0).setPreferredWidth(30); // Ширина колонки нумерации
        hexTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Ширина колонки адреса
        hexTable.getColumnModel().getColumn(2).setPreferredWidth(BYTES_PER_LINE * 3 + 10); // Ширина колонки hex

        // Создаем текстовую область
        textArea = new JTextArea(15, 20);
        textArea.setEditable(false); // Запрещаем редактирование
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        textArea.setTabSize(10);



        // Добавляем текстовую область, выбор размера блока и
        // десятичное отображение в отдельное окно
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(dataSizeComboBox, BorderLayout.NORTH); // Добавление JComboBox
        rightPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        rightPanel.add(decimalValueLabel, BorderLayout.SOUTH); // Добавление нового JLabel

        // Добавляем все области в основное окно
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(hexTable), BorderLayout.NORTH);
        getContentPane().add(rightPanel, BorderLayout.EAST);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
        getContentPane().add(separator, BorderLayout.CENTER);


        // Создаем диалоговое окно выбора файла
        JFileChooser fileChooser = new JFileChooser();

        setVisible(true);

    }

    // Метод для изменения размеров таблицы
    private void resizeTable() {
        // Обновляем модель таблицы
        tableModel.setColumnCount(BYTES_PER_LINE + 3);
        String[] columnNames = new String[BYTES_PER_LINE + 3]; // +3 для нумерации строк и заголовка
        columnNames[0] = "No";   // Нумерация строк
        columnNames[1] = "Address"; // Адрес
        columnNames[2] = "Hex"; // Заголовок для hex данных
        for (int i = 0; i < BYTES_PER_LINE; i++) {
            columnNames[i + 3] = String.valueOf(i + 1); // Нумерация столбцов (1, 2, 3 ...)
        }
        tableModel.setColumnIdentifiers(columnNames);

        // Устанавливаем новое количество строк
        tableModel.setRowCount(rows);

        // Обновляем данные в таблице
        if (fileChannel != null) {
            updateHexTable(tableModel, fileChannel, fileSize, currentPosition);
        }
    }
    //a
    private void updateHexTable(DefaultTableModel tableModel, FileChannel fileChannel, long fileSize, long currentPosition) {
        try {
            // Очищаем таблицу
            tableModel.setRowCount(0);

            // Читаем данные из файла
            ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long remainingBytes = fileSize - currentPosition;
            long currentLine = 0;
            while (remainingBytes > 0) {
                int bytesToRead = (int) Math.min(BYTES_PER_LINE, remainingBytes);
                fileChannel.position(currentPosition + currentLine * BYTES_PER_LINE);
                fileChannel.read(buffer);
                buffer.flip();

                // Добавляем новую строку в таблицу
                Object[] rowData = new Object[BYTES_PER_LINE + 3];
                rowData[0] = currentLine; // Нумерация строк
                rowData[1] = String.format("%08X", currentPosition + currentLine * BYTES_PER_LINE); // Адрес
                rowData[2] = "Hex"; // Заголовок для hex данных
                for (int i = 0; i < bytesToRead; i++) {
                    rowData[i + 3] = String.format("%02X", buffer.get() & 0xFF); // Шестнадцатеричное значение
                }
                tableModel.addRow(rowData);

                currentLine++;
                remainingBytes -= BYTES_PER_LINE;
                buffer.clear();
            }

            // Добавляем пустые строки, если нужно
            while (currentLine < rows) {
                Object[] rowData = new Object[BYTES_PER_LINE + 3];
                rowData[0] = currentLine; // Нумерация строк
                rowData[1] = String.format("%08X", currentPosition + currentLine * BYTES_PER_LINE); // Адрес
                rowData[2] = "Hex"; // Заголовок для hex данных
                Arrays.fill(rowData, 3, BYTES_PER_LINE + 3, "00"); // Заполняем пустые ячейки нулями
                tableModel.addRow(rowData);
                currentLine++;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(HexEditor.this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void search(FileChannel fileChannel) {
        if (fileChannel == null) {
            JOptionPane.showMessageDialog(this, "No file open", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String searchPattern = searchField.getText().trim();

        // Проверяем, не пуст ли ввод
        if (searchPattern.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search pattern", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Преобразуем поисковый шаблон в байты
            byte[] searchBytes = hexStringToBytes(searchPattern);

            // Определяем, использовать точное совпадение или маску
            boolean useMask = maskMatchButton.isSelected();

            // Выполняем поиск
            long foundAddress = findBytes(searchBytes, currentPosition, useMask);

            if (foundAddress != -1) {
                //  Отображаем адрес найденного совпадения
                JOptionPane.showMessageDialog(this, "Found at address: " + String.format("%08X", foundAddress), "Search Result", JOptionPane.INFORMATION_MESSAGE);

                // Перемещаем курсор в таблице на найденную строку
                int row = (int) ((foundAddress - currentPosition) / BYTES_PER_LINE);
                hexTable.getSelectionModel().setSelectionInterval(row, row);
            } else {
                JOptionPane.showMessageDialog(this, "Not found", "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalArgumentException | IOException e) {
            JOptionPane.showMessageDialog(this, "Invalid search pattern", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для преобразования шестнадцатеричной строки в массив байт
    private byte[] hexStringToBytes(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length");
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            String hexValue = hexString.substring(i, i + 2);
            bytes[i / 2] = (byte) Integer.parseInt(hexValue, 16);
        }
        return bytes;
    }

    // Метод для поиска последовательности байт в файле
    private long findBytes(byte[] searchBytes, long startPosition, boolean useMask) throws IOException {
        fileChannel.position(startPosition);
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_LINE);
        while (fileChannel.position() < fileChannel.size()) {
            fileChannel.read(buffer);
            buffer.flip();
            if (useMask) {
                // Поиск по маске
                if (matchMask(buffer, searchBytes)) {
                    return fileChannel.position() - BYTES_PER_LINE;
                }
            } else {
                // Точное совпадение
                if (matchBytes(buffer, searchBytes)) {
                    return fileChannel.position() - BYTES_PER_LINE;
                }
            }
            buffer.clear();
        }
        return -1; // Совпадение не найдено
    }

    // Метод для сравнения байт с точным совпадением
    private boolean matchBytes(ByteBuffer buffer, byte[] searchBytes) {
        if (buffer.remaining() < searchBytes.length) {
            return false;
        }
        for (int i = 0; i < searchBytes.length; i++) {
            if (buffer.get() != searchBytes[i]) {
                return false;
            }
        }
        return true;
    }

    // Метод для сравнения байт с использованием маски
    private boolean matchMask(ByteBuffer buffer, byte[] searchBytes) {
        if (buffer.remaining() < searchBytes.length) {
            return false;
        }
        for (int i = 0; i < searchBytes.length; i++) {
            if ((searchBytes[i] & 0xFF) != 0 && (buffer.get() & 0xFF) != (searchBytes[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}