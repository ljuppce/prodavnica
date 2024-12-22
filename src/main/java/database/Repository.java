package database;

import java.sql.*;

public class Repository {

    public static void addCustomer(String name, String email) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO customers (name, email) VALUES (?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }

    public static ResultSet getAllCustomers() throws SQLException {
        Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT * FROM customers");
    }

    public static void addProduct(String name, double price, int stock) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, stock);
            stmt.executeUpdate();
        }
    }

    public static ResultSet getAllProducts() throws SQLException {
        Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT * FROM products");
    }

    public static void addSale(int customerId, int productId, int quantity) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmtSale = conn.prepareStatement("INSERT INTO sales (customer_id, product_id, quantity) VALUES (?, ?, ?)");
             PreparedStatement stmtUpdateProduct = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?");
             PreparedStatement stmtUpdateCustomer = conn.prepareStatement("UPDATE customers SET balance = balance - ? WHERE id = ?")) {

            // Insert sale into sales table
            stmtSale.setInt(1, customerId);
            stmtSale.setInt(2, productId);
            stmtSale.setInt(3, quantity);
            stmtSale.executeUpdate();

            // Update product stock
            stmtUpdateProduct.setInt(1, quantity);
            stmtUpdateProduct.setInt(2, productId);
            stmtUpdateProduct.executeUpdate();

            // Update customer balance
            double totalPrice = getProductPrice(productId) * quantity;
            stmtUpdateCustomer.setDouble(1, totalPrice);
            stmtUpdateCustomer.setInt(2, customerId);
            stmtUpdateCustomer.executeUpdate();
        }
    }

    private static double getProductPrice(int productId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT price FROM products WHERE id = ?")) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
            return 0;
        }
    }
}
