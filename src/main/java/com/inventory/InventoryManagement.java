
package com.inventory;

import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {

    static final String URL = "jdbc:mysql://localhost:3306/retail_db";
    static final String USER = "root";
    static final String PASSWORD = "9450";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL successfully!");

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT =====");
                System.out.println("1. Insert Products");
                System.out.println("2. Retrieve Products");
                System.out.println("3. Update Stock");
                System.out.println("4. Search and Update Price");
                System.out.println("5. Total Value and Delete Product");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        insertProducts();
                        break;
                    case 2:
                        retrieveProducts();
                        break;
                    case 3:
                        updateStock();
                        break;
                    case 4:
                        updatePrice();
                        break;
                    case 5:
                        totalValueAndDelete();
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // A. Insert at least three products
    static void insertProducts() throws SQLException {

        String sql = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.print("Product ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Product Name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Price: ");
                double price = sc.nextDouble();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    ps.executeUpdate();
                    System.out.println("Product inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Product ID!");
                }
            }
        }
    }

    // B. Retrieve by category and quantity less than 20
    static void retrieveProducts() throws SQLException {

        sc.nextLine();
        System.out.print("Enter category: ");
        String category = sc.nextLine();

        String sql = "SELECT * FROM Product WHERE category = ? " +
                "AND quantity < 20 ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                displayProduct(rs);
            }
            rs.close();
        }
    }

    // C. Add stock to existing quantity
    static void updateStock() throws SQLException {

        System.out.print("Enter Product ID: ");
        int id = sc.nextInt();

        System.out.print("Enter quantity to add: ");
        int quantity = sc.nextInt();

        String sql = "UPDATE Product SET quantity = quantity + ? " +
                "WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Stock updated successfully!");
            else
                System.out.println("Product does not exist!");
        }
    }

    // D. Search and update price
    static void updatePrice() throws SQLException {

        System.out.print("Enter Product ID: ");
        int id = sc.nextInt();

        String select = "SELECT * FROM Product WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(select)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                displayProduct(rs);

                System.out.print("Enter new price: ");
                double price = sc.nextDouble();

                if (price > 0) {

                    String update = "UPDATE Product SET price = ? " +
                            "WHERE product_id = ?";

                    try (PreparedStatement updatePs =
                                 con.prepareStatement(update)) {

                        updatePs.setDouble(1, price);
                        updatePs.setInt(2, id);
                        updatePs.executeUpdate();

                        System.out.println("Price updated successfully!");

                    }

                    try (PreparedStatement check =
                                 con.prepareStatement(select)) {
                        check.setInt(1, id);
                        ResultSet updated = check.executeQuery();

                        if (updated.next())
                            displayProduct(updated);

                        updated.close();
                    }

                } else {
                    System.out.println("Price must be greater than zero!");
                }

            } else {
                System.out.println("Product not found!");
            }

            rs.close();
        }
    }

    // E. Calculate total value and delete if quantity is zero
    static void totalValueAndDelete() throws SQLException {

        String sql = "SELECT SUM(quantity * price) AS total FROM Product";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Inventory Value: " +
                        rs.getDouble("total"));
            }
        }

        System.out.print("Enter Product ID to delete: ");
        int id = sc.nextInt();

        String delete = "DELETE FROM Product " +
                "WHERE product_id = ? AND quantity = 0";

        try (PreparedStatement ps = con.prepareStatement(delete)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Product deleted successfully!");
            else
                System.out.println("Cannot delete: Product not found " +
                        "or quantity is not zero!");
        }
    }

    // Display product details
    static void displayProduct(ResultSet rs) throws SQLException {

        System.out.println(
                "ID: " + rs.getInt("product_id") +
                        " | Name: " + rs.getString("product_name") +
                        " | Category: " + rs.getString("category") +
                        " | Quantity: " + rs.getInt("quantity") +
                        " | Price: " + rs.getDouble("price")
        );
    }
}
