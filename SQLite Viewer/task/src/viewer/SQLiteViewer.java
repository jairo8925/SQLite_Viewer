package viewer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLiteViewer extends JFrame {

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
        add(queryTextArea);

        JButton executeQueryButton = new JButton("Execute");
        executeQueryButton.setName("ExecuteQueryButton");
        executeQueryButton.setBounds(520, 120, 135, 40);
        add(executeQueryButton);

        JTable table = new JTable();
        table.setName("Table");
        table.setBounds(25, 275, 630, 550);
        add(table);

        add(new JScrollPane(table));


        openFileButton.addActionListener(e -> {
            List<String> tables = new ArrayList<>();
            tablesComboBox.removeAllItems();
            String filename = nameTextField.getText();
            String url = "jdbc:sqlite:" + filename;
            String sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%'";
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }
            for (String item : tables) {
                tablesComboBox.addItem(item);
            }
        });

        tablesComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                queryTextArea.setText("SELECT * FROM " + e.getItem().toString() + ";");
            }
            if (e.getStateChange() == ItemEvent.DESELECTED){
                queryTextArea.setText("");
            }
        });

        executeQueryButton.addActionListener(e -> {
            List<String> columns = new ArrayList<>();
            String filename = nameTextField.getText();
            String url = "jdbc:sqlite:" + filename;

            String com = "SELECT * FROM " + tablesComboBox.getSelectedItem() + ";";
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(com)) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                   columns.add(rsmd.getColumnName(i));
                }
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }

            Object[][] data = null;

            String sql = queryTextArea.getText();
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)) {
                data = new Object[3][5];
                int counter = 0;
                while (rs.next()) {
                    for (int i = 0; i < columns.size(); i++) {
                        data[counter][i] = rs.getString(columns.get(i));
                        // System.out.print(data[counter][i] + " ");
                        // System.out.println(rs.getString(columns.get(i)));
                    }
                    // System.out.println();
                    counter++;
                    System.out.println(Arrays.deepToString(data));
                }
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }

            DefaultTableModel model = new DefaultTableModel(data, columns.toArray());
            table.setModel(model);
            System.out.println(table.getColumnCount());
            System.out.println(table.getRowCount());
        });
    }
}
