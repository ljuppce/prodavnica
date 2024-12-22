package controllers;

import database.DBConnection;
import models.Sale;
import models.Customer;
import models.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/sales")
public class SaleController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rsCustomers = stmt.executeQuery("SELECT * FROM customers");
            ResultSet rsProducts = stmt.executeQuery("SELECT * FROM products");

            request.setAttribute("customers", rsCustomers);
            request.setAttribute("products", rsProducts);
            request.getRequestDispatcher("/WEB-INF/views/sales.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int customerId = Integer.parseInt(request.getParameter("customerId"));
        int productId = Integer.parseInt(request.getParameter("productId"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

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

            // Update customer balance (simplified)
            double totalPrice = getProductPrice(productId) * quantity;
            stmtUpdateCustomer.setDouble(1, totalPrice);
            stmtUpdateCustomer.setInt(2, customerId);
            stmtUpdateCustomer.executeUpdate();
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        response.sendRedirect("sales");
    }

    private double getProductPrice(int productId) throws SQLException {
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
