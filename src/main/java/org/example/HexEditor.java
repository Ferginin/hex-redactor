package org.example;

import org.example.tableFuncs.*;
import org.example.tableSettings.*;
import org.example.workers.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.util.*;

public class HexEditor extends JFrame {

    private static final String[] DATA_SIZES = {"Free choosing", "2 bytes", "4 bytes", "8 bytes"};
    private int BYTES_PER_LINE = 16;
    private byte[] fileContent; // Хранит весь файл в памяти

    private CustomTable hexTable;
    private final DefaultTableModel tableModel;
    private FileChannel fileChannel;
    private JTextArea textArea;
    private final JComboBox<String> dataSizeComboBox;
    private boolean changesMade = false;
    private boolean initializationComplete = false; // Флаг для отслеживания завершения инициализации
    private long fileSize;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private int currentPage = 0;
    private final int pageSize = 100; // Количество строк на странице
    private JLabel pageLabel;
    private JTextField currentPageField;
    private final ByteBuffer buffer;


    public HexEditor() {
        JFrame frame = new JFrame("Hex Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("images/hexThing.png");
        frame.setIconImage(icon.getImage());
        frame.setSize(1400, 800);
        frame.setLocationRelativeTo(null);

        buffer = new ByteBuffer();

        // Создаем разделитель
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1, 10));

        // Создаем меню
        JMenuBar menuBar = getjMenuBar();
        frame.setJMenuBar(menuBar);

        // Создаем элементы управления для изменения размеров таблицы
        JLabel colsLabel = new JLabel("Cols: ");
        JTextField colsField = new JTextField("16");

        // Кнопка для изменения размеров
        JButton resizeButton = getResizeButton(colsField);

        // Создаем выпадающий список для выбора размера данных
        dataSizeComboBox = new JComboBox<>(DATA_SIZES);
        dataSizeComboBox.setSelectedIndex(0); // По умолчанию 1 байт
        dataSizeComboBox.addActionListener(e -> hexTable.setSelectionMode(dataSizeComboBox.getSelectedIndex()));


        // Создаем модель таблицы
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3; // Разрешаем редактирование только колонок данных
            }
        };

        // Добавляем слушатель событий к модели таблицы
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                if (initializationComplete) {
                    new FileContentUpdater(getThis()).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
                    TextAreaHandler.updateTextArea(tableModel, textArea);
                    changesMade = true;
                }
            }
        });

        // Создаем текстовую область
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false); // Запрещаем редактирование
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        textArea.setTabSize(10);

        // Создаем таблицу
        hexTable = new CustomTable(tableModel) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }

            @Override
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (columnIndex >= 3) { // Разрешаем выбор только колонок данных
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
                }
            }
        };
        hexTable.setCellSelectionEnabled(true);
        hexTable.getTableHeader().setReorderingAllowed(false);

        new SelectionHandler(textArea, hexTable);

        // Создаем элементы управления для поиска
        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField("Пример: 52 61 ? 42 *", 20);
        // Устанавливаем серый цвет для подсказки
        searchField.setForeground(Color.GRAY);
        // обработка поля поиска
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Пример: 52 61 ? 42 *")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Пример: 52 61 ? 42 *");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        JRadioButton exactMatchButton = new JRadioButton("Exact Match");
        JRadioButton maskMatchButton = new JRadioButton("Mask Match");
        SearchBytes byteSearch = new SearchBytes(hexTable, searchField, exactMatchButton, maskMatchButton, textArea);
        ButtonGroup searchGroup = new ButtonGroup();
        searchGroup.add(exactMatchButton);
        searchGroup.add(maskMatchButton);
        exactMatchButton.setSelected(true);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> byteSearch.Search());

        // Создаем JTextPane для отображения выделенных данных
        JTextArea selectionView = new JTextArea();
        JScrollPane scrollPaneDecimalValue = new JScrollPane(selectionView);
        scrollPaneDecimalValue.setPreferredSize(new Dimension(300, 60));
        new DecimalValue(hexTable, selectionView);

        // Создаем нижнюю панель с GridBagLayout
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Отступы между элементами

        // Массив элементов для добавления на панель
        Component[] components = {
                searchLabel, searchField, exactMatchButton, maskMatchButton, searchButton,
                colsLabel, colsField, resizeButton,
                dataSizeComboBox, scrollPaneDecimalValue
        };

        // Добавляем элементы на нижнюю панель
        for (int i = 0; i < components.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            bottomPanel.add(components[i], gbc);
        }

        // Добавляем нижнюю панель на основную панель
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // обработчик закрытия программы
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (changesMade) {
                    changesMade = false;
                    int result = JOptionPane.showConfirmDialog(frame,
                            "Do you want to save the changes?",
                            "Save Changes",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            new FileContentUpdater(getThis()).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
                            new SaveChanges(fileChannel).save(false, getThis());
                            System.exit(0);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (result == JOptionPane.NO_OPTION) {
                        System.exit(0);
                    } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Не закрывать окно, если пользователь отменил или закрыл окно
                    }
                } else {
                    System.exit(0);
                }
            }
        });

        String[] columnNames = new String[BYTES_PER_LINE + 3]; // +3 для нумерации строк и заголовка
        columnNames[0] = "No";   // Нумерация строк
        columnNames[1] = "Address"; // Адрес
        columnNames[2] = "Hex"; // Заголовок для hex данных
        for (int i = 0; i < BYTES_PER_LINE; i++) {
            columnNames[i + 3] = String.valueOf(i + 1); // Нумерация столбцов (1, 2, 3 ...)
        }
        tableModel.setColumnIdentifiers(columnNames);

        // Устанавливаем рендерер для всех колонок таблицы
        for (int i = 0; i < hexTable.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setCellRenderer(new HexTableCellRenderer());
        }

        // Устанавливаем редактор для всех колонок таблицы
        for (int i = 3; i < hexTable.getColumnCount(); i++) {
            hexTable.getColumnModel().getColumn(i).setCellEditor(new HexTableCellEditor());
        }

        // устанавливаем сочетания клавиш
        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control C"), "copy");
        hexTable.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    copySelection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control X"), "cutWithZeroing");
        hexTable.getActionMap().put("cutWithZeroing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cutSelectionWithZeroing();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control shift X"), "cutWithShift");
        hexTable.getActionMap().put("cutWithShift", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cutSelectionWithShift();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                    Set<Point> selectedCells = hexTable.getSelectedCells();
                    selectedCells.clear();
                    updateCurrentPageLabel(currentPage + 1);
                    updatePageLabel();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "deleteWithZeroing");
        hexTable.getActionMap().put("deleteWithZeroing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteSelectionWithZeroing();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control DELETE"), "deleteWithShift");
        hexTable.getActionMap().put("deleteWithShift", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteSelectionWithShift();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                    Set<Point> selectedCells = hexTable.getSelectedCells();
                    selectedCells.clear();
                    updateCurrentPageLabel(currentPage + 1);
                    updatePageLabel();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pasteWithReplace");
        hexTable.getActionMap().put("pasteWithReplace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    pasteSelectionWithReplace();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getInputMap().put(KeyStroke.getKeyStroke("control shift V"), "pasteWithoutReplace");
        hexTable.getActionMap().put("pasteWithoutReplace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    pasteSelectionWithoutReplace();
                    new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, getThis()).execute();
                    updateCurrentPageLabel(currentPage + 1);
                    updatePageLabel();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        hexTable.getColumnModel().getColumn(0).setPreferredWidth(40); // Ширина колонки нумерации
        hexTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Ширина колонки адреса
        hexTable.getColumnModel().getColumn(2).setPreferredWidth(BYTES_PER_LINE * 3 + 10); // Ширина колонки hex

        // Добавляем все области в основное окно
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(hexTable), BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(mainPanel, BorderLayout.SOUTH);
        frame.add(separator, BorderLayout.EAST);

        frame.setVisible(true);
        initializationComplete = true;
    }

    private JButton getResizeButton(JTextField colsField) {
        JButton resizeButton = new JButton("Resize");
        resizeButton.addActionListener(e -> {
            try {
                if(Integer.parseInt(colsField.getText()) != BYTES_PER_LINE && Integer.parseInt(colsField.getText()) > 0) {
                    BYTES_PER_LINE = Integer.parseInt(colsField.getText());
                    Set<Point> selectedCells = hexTable.getSelectedCells();
                    selectedCells.clear();

                    ResizeTable resizer = new ResizeTable();
                    currentPage = 0;
                    if (textArea != null) {
                        textArea.setText(""); // Очищаем текстовую область
                    }
                    resizer.resize(tableModel, BYTES_PER_LINE);
                    loadPage(currentPage);
                    updateCurrentPageLabel(currentPage + 1);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(HexEditor.this, "Invalid input", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return resizeButton;
    }

    private JMenuBar getjMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> {
            OpenFile openObject = new OpenFile(fileChannel, fileSize);
            openObject.open(tableModel, textArea, this);
            fileChannel = openObject.getFileChannel();
            fileSize = openObject.getFileSize();
        });
        fileMenu.add(openItem);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            try {
                new FileContentUpdater(this).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
                new SaveChanges(fileChannel).save(false, this);
                changesMade = false;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(HexEditor.this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        fileMenu.add(saveItem);
        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(e -> {
            try {
                new FileContentUpdater(this).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
                new SaveChanges(fileChannel).save(true, this);
                changesMade = false;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(HexEditor.this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        fileMenu.add(saveAsItem);
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> System.exit(0));
        fileMenu.add(closeItem);

        // Создаем кнопки для навигации по страницам
        prevPageButton = new JButton("Previous Page");
        nextPageButton = new JButton("Next Page");
        pageLabel = new JLabel("Page 1/" + updateNavigationButtons());
        // создаем поле для ввода номера страницы
        currentPageField = new JTextField(6);
        currentPageField.setHorizontalAlignment(JTextField.CENTER);
        currentPageField.setText("1");
        // Применяем фильтр к текстовому полю
        AbstractDocument doc = (AbstractDocument) currentPageField.getDocument();
        doc.setDocumentFilter(new DigitOnlyDocumentFilter());

        // Добавляем слушатели для кнопок навигации
        prevPageButton.addActionListener(e -> {
            new FileContentUpdater(this).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
            Set<Point> selectedCells = hexTable.getSelectedCells();
            selectedCells.clear();
            loadPage(currentPage - 1);
            updateCurrentPageLabel(currentPage + 1);
        });
        nextPageButton.addActionListener(e -> {
            new FileContentUpdater(this).updateFileContent(tableModel, BYTES_PER_LINE, currentPage, pageSize);
            Set<Point> selectedCells = hexTable.getSelectedCells();
            selectedCells.clear();
            loadPage(currentPage + 1);
            updateCurrentPageLabel(currentPage + 1);
        });
        // слушатель для поля с номером страницы
        currentPageField.addActionListener(e -> {
            try {
                currentPage = Integer.parseInt(currentPageField.getText());
                int totalPages = updateNavigationButtons();
                if (currentPage > 0 && currentPage <= totalPages) {
                    Set<Point> selectedCells = hexTable.getSelectedCells();
                    selectedCells.clear();
                    loadPage(currentPage - 1);
                } else {
                    JOptionPane.showMessageDialog(null, "Введите корректный номер страницы.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Некорректный номер страницы.");
            }
        });

        // Добавляем кнопки на панель
        JPanel navigationPanel = new JPanel(new FlowLayout());
        navigationPanel.add(prevPageButton);
        navigationPanel.add(nextPageButton);
        navigationPanel.add(pageLabel);
        navigationPanel.add(currentPageField);
        menuBar.add(fileMenu);
        menuBar.add(navigationPanel);
        return menuBar;
    }

    private  void copySelection() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        new CopyWorker(buffer, this).copy(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // вырезка с обнулением
    private void cutSelectionWithZeroing() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        new CutWorker(buffer, this).cut(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // вырезка со сдвигом
    private void cutSelectionWithShift() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        new CutWithShiftWorker(buffer, this).cutBytes(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // удаление с обнулением
    private void deleteSelectionWithZeroing() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        new DeleteWorker(this).removeBytesWithPadding(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // удаление со сдвигом
    private void deleteSelectionWithShift() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }

        new DeleteWithShiftWorker(this).removeBytes(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // вставка с заменой
    private void pasteSelectionWithReplace() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }
        new PasteWorker(buffer, this).replaceBytes(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    // вставка со сдвигом
    private void pasteSelectionWithoutReplace() throws IOException {
        Set<Point> selectedCells = hexTable.getSelectedCells();
        if (selectedCells.isEmpty()) {
            return;
        }
        new PasteWithShiftWorker(buffer, this).insertBytes(selectedCells, BYTES_PER_LINE, currentPage, pageSize);
    }

    public void loadPage(int page) {
        if (page < 0) {
            page = 0;
        }
        currentPage = page;
        new FileLoader(BYTES_PER_LINE, tableModel, textArea, fileSize, currentPage, pageSize, this).execute();
        updatePageLabel();
        updateNavigationButtons();
    }

    public void updateCurrentPageLabel(int currentPage) {
        currentPageField.setText(String.valueOf(currentPage));
    }

    public void updatePageLabel() {
        pageLabel.setText("Page " + (currentPage + 1) + "/" + updateNavigationButtons());
    }

    public int updateNavigationButtons() {
        int totalPages = (int) Math.ceil((double) fileSize / (pageSize * BYTES_PER_LINE));
        prevPageButton.setEnabled(currentPage > 0);
        nextPageButton.setEnabled(currentPage < totalPages - 1);

        return totalPages;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public HexEditor getThis() {
        return this;
    }
}