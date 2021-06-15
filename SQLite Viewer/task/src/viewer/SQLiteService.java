package viewer;

import org.sqlite.SQLiteDataSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteService {
    private final SQLiteDataSource dataSource = new SQLiteDataSource();

    public SQLiteService(String url) {
        dataSource.setUrl("jdbc:sqlite:" + url);
    }

    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(new Frame(), exception.getMessage());
        }
        return tables;
    }

    public DefaultTableModel executeQuery(String query) {
        String[] columns = null;
        List<String[]> lst = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(query)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = rsmd.getColumnName(i + 1);
            }
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(columns[i]);
                }
                lst.add(row);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(new Frame(), exception.getMessage());
        }
        return new DefaultTableModel(lst.toArray(String[][]::new), columns);
    }
}
