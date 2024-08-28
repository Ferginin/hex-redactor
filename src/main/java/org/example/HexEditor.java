package org.example;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.channels.FileChannel;

public class HexEditor extends JFrame {

    private static final int BYTES_PER_LINE = 16;
    private JTable hexTable;
    private DefaultTableModel tableModel;
    private FileChannel fileChannel;
    private long fileSize;
    private long currentPosition;
    private int selectedRow = -1;
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private JLabel decimalValueLabel;

    public HexEditor() {
        super("Hex Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

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

        // Создаем таблицу
        hexTable = new JTable(tableModel) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        hexTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hexTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && hexTable.getSelectedRow() != -1) {
                    selectedRow = hexTable.getSelectedRow();
                    int selectedColumn = hexTable.getSelectedColumn();
                    if (selectedRow >= 0 && selectedColumn >= 3) {
                        new updateDecimalValue(selectedRow, selectedColumn - 2, tableModel, decimalValueLabel);
                    }
                }
            }
        });

        hexTable.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedRow >= 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    new saveByte(selectedRow, hexTable.getSelectedColumn() - 2, BYTES_PER_LINE, currentPosition, tableModel, fileChannel, hexTable, decimalValueLabel);
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

        // Создаем JLabel для отображения значения байта
        decimalValueLabel = new JLabel("Decimal Value: ");

        // Добавляем таблицу, текстовую область и JLabel в основное окно
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        rightPanel.add(decimalValueLabel, BorderLayout.SOUTH); // Добавление нового JLabel

        // Добавляем таблицу и текстовую область в основное окно
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(hexTable), BorderLayout.NORTH);
        //getContentPane().add(new JScrollPane(textArea), BorderLayout.SOUTH);
        getContentPane().add(rightPanel, BorderLayout.EAST);

        // Создаем диалоговое окно выбора файла
        JFileChooser fileChooser = new JFileChooser();

        setVisible(true);
    }
}