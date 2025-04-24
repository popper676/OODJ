import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class InventoryManagerUI {
    JFrame frame;
    JTextArea displayArea;
    JButton viewItemsBtn, updateStockBtn, lowStockAlertBtn, viewPOsBtn, generateReportBtn;

    public InventoryManagerUI() {
        frame = new JFrame("Inventory Manager Dashboard");
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        frame.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        viewItemsBtn = new JButton("View Items");
        updateStockBtn = new JButton("Update Stock");
        lowStockAlertBtn = new JButton("Low Stock Alerts");
        viewPOsBtn = new JButton("View POs");
        generateReportBtn = new JButton("Stock Report");

        buttonPanel.add(viewItemsBtn);
        buttonPanel.add(updateStockBtn);
        buttonPanel.add(lowStockAlertBtn);
        buttonPanel.add(viewPOsBtn);
        buttonPanel.add(generateReportBtn);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        viewItemsBtn.addActionListener(e -> displayItems());
        updateStockBtn.addActionListener(e -> updateStock());
        lowStockAlertBtn.addActionListener(e -> showLowStockAlerts());
        viewPOsBtn.addActionListener(e -> viewPOs());
        generateReportBtn.addActionListener(e -> generateStockReport());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Method to display all items from the database
    void displayItems() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT item_id, item_name, unit_price, stock_qty FROM items";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            displayArea.setText("--- Item List ---\n");
            while (rs.next()) {
                displayArea.append(rs.getInt("item_id") + " | " +
                                   rs.getString("item_name") + " | " +
                                   rs.getDouble("unit_price") + " | " +
                                   rs.getInt("stock_qty") + "\n");
            }
        } catch (SQLException e) {
            displayArea.setText("Error reading items from database.");
            e.printStackTrace();
        }
    }

    // Method to update stock of an item
    void updateStock() {
        String itemID = JOptionPane.showInputDialog("Enter Item ID to update:");
        String quantity = JOptionPane.showInputDialog("Enter new quantity:");

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE items SET stock_qty = ? WHERE item_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(quantity));
            stmt.setInt(2, Integer.parseInt(itemID));
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                displayArea.setText("Stock updated successfully.");
            } else {
                displayArea.setText("Error: Item not found.");
            }
        } catch (SQLException e) {
            displayArea.setText("Error updating stock.");
            e.printStackTrace();
        }
    }

    // Method to show items with low stock (less than 10)
    void showLowStockAlerts() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT item_id, item_name, stock_qty FROM items WHERE stock_qty < 10";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            displayArea.setText("--- Low Stock Items ---\n");
            while (rs.next()) {
                displayArea.append(rs.getInt("item_id") + " | " +
                                   rs.getString("item_name") + " | " +
                                   rs.getInt("stock_qty") + "\n");
            }
        } catch (SQLException e) {
            displayArea.setText("Error reading low stock items.");
            e.printStackTrace();
        }
    }

    // Method to display all purchase orders
    void viewPOs() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT po_id, item_id, quantity, order_date FROM purchase_orders";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            displayArea.setText("--- Purchase Orders ---\n");
            while (rs.next()) {
                displayArea.append(rs.getInt("po_id") + " | " +
                                   rs.getInt("item_id") + " | " +
                                   rs.getInt("quantity") + " | " +
                                   rs.getDate("order_date") + "\n");
            }
        } catch (SQLException e) {
            displayArea.setText("Error reading purchase orders.");
            e.printStackTrace();
        }
    }

    // Method to generate a stock report
    void generateStockReport() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT item_id, item_name, stock_qty FROM items";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("stock_report.txt"))) {
                while (rs.next()) {
                    writer.write(rs.getInt("item_id") + " | " +
                                 rs.getString("item_name") + " | " +
                                 rs.getInt("stock_qty") + "\n");
                }
                displayArea.setText("Stock report generated: stock_report.txt");
            } catch (IOException e) {
                displayArea.setText("Error generating report.");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            displayArea.setText("Error reading items for report.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new InventoryManagerUI();
    }
}
