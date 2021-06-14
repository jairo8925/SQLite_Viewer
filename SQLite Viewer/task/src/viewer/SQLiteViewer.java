package viewer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
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
        add(table);

        add(new JScrollPane(table));

        openFileButton.addActionListener(e -> {
            executeQueryButton.setEnabled(false);
            queryTextArea.setEnabled(false);
            String filename = nameTextField.getText();
            File file = new File (filename);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(new Frame(), "File doesn't exist!");
            } else {
                String url = "jdbc:sqlite:" + filename;
                String sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%'";
                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {
                    tablesComboBox.removeAllItems();
                    while (rs.next()) {
                        tablesComboBox.addItem(rs.getString("name"));
                    }
                    queryTextArea.setText("SELECT * FROM " + tablesComboBox.getSelectedItem() + ";");
                } catch (SQLException exception) {
                    System.out.println(exception.getMessage());
                }
                queryTextArea.setEnabled(true);
                executeQueryButton.setEnabled(true);
            }
        });

        tablesComboBox.addItemListener(e -> {
            queryTextArea.setText("SELECT * FROM " + e.getItem().toString() + ";");
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

            List<String[]> lst = new ArrayList<>();

            String sql = queryTextArea.getText();
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String[] row = new String[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        row[i] = rs.getString(columns.get(i));
                    }
                    lst.add(row);
                }
            } catch (SQLException exception) {
                System.out.println(exception.getMessage());
            }

            DefaultTableModel model = new DefaultTableModel(lst.toArray(Object[][]::new), columns.toArray());
            table.setModel(model);
        });
    }
}
