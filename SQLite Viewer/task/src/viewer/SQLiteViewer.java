package viewer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class SQLiteViewer extends JFrame {

    private static SQLiteService service;
    private static String filename;

    public SQLiteViewer() {
        super("SQLite Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 900);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        JTextField nameTextField = new JTextField();
        nameTextField.setName("FileNameTextField");
        nameTextField.setBounds(25, 20, 500, 30);
        add(nameTextField);

        JButton openFileButton = new JButton("Open");
        openFileButton.setName("OpenFileButton");
        openFileButton.setBounds(550, 20, 110, 30);
        add(openFileButton);

        JComboBox<String> tablesComboBox = new JComboBox<>();
        tablesComboBox.setName("TablesComboBox");
        tablesComboBox.setBounds(25, 70, 635, 25);
        add(tablesComboBox);

        JTextArea queryTextArea = new JTextArea();
        queryTextArea.setName("QueryTextArea");
        queryTextArea.setBounds(25, 120, 480, 120);
        queryTextArea.setEnabled(false);
        add(queryTextArea);

        JButton executeQueryButton = new JButton("Execute");
        executeQueryButton.setName("ExecuteQueryButton");
        executeQueryButton.setBounds(520, 120, 135, 40);
        executeQueryButton.setEnabled(false);
        add(executeQueryButton);

        JTable table = new JTable();
        table.setName("Table");
        table.setBounds(25, 275, 630, 550);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(table);

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(25, 275, 630, 550);
        scrollPane.setPreferredSize(new Dimension(630, 550));
        scrollPane.setSize(630,550);
        getContentPane().add(scrollPane);
        add(scrollPane);

        openFileButton.addActionListener(e -> {
            tablesComboBox.removeAllItems();
            executeQueryButton.setEnabled(false);
            queryTextArea.setEnabled(false);
            filename = nameTextField.getText().trim();
            File file = new File (filename);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(new Frame(), "File doesn't exist!");
            } else {
                service = new SQLiteService(filename); // establish connection to database
                List<String> tables = service.getTables(); // get all tables of database
                tables.forEach(tablesComboBox::addItem); // add all tables to selection box
                queryTextArea.setEnabled(!tables.isEmpty());
                executeQueryButton.setEnabled(!tables.isEmpty());
            }
        });

        tablesComboBox.addItemListener(itemEvent -> {
            String tableName = itemEvent.getItem().toString();
            queryTextArea.setText("SELECT * FROM " + tableName + ";");
        });

        executeQueryButton.addActionListener(e -> {
            if (service == null) {
                throw new IllegalStateException();
            }

            TableModel model = service.executeQuery(queryTextArea.getText());

            if (model == null) {
                throw new IllegalStateException();
            }

            table.setModel(model);
            for (int i = 0; i < model.getColumnCount(); i++) {
                TableColumn column = table.getColumnModel().getColumn(i);
                column.setMinWidth(175);
                column.setMaxWidth(520);
                column.setPreferredWidth(200);
            }
        });
    }
}
