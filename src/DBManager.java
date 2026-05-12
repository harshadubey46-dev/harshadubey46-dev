package com.yourname.expensetracker;

//import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DBManager {

    private static final String URL = "jdbc:sqlite:expenses.db";
    public static int currentUserId = -1;

    static {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT)
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category TEXT,
                amount REAL,
                date TEXT)
            """);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 🔐 AUTH
    public static boolean registerUser(String u, String p) {
        try (Connection c = DriverManager.getConnection(URL)) {
        	String hash = p;
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users(username,password) VALUES(?,?)");
            ps.setString(1, u);
            ps.setString(2, hash);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean login(String u, String p) {
        try (Connection c = DriverManager.getConnection(URL)) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM users WHERE username=?");
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && p.equals(rs.getString("password"))) {
                currentUserId = rs.getInt("id");
                return true;
            }
        } catch (Exception e) { }
        return false;
    }

    // 📊 EXPENSES
    public static List<Expense> loadExpenses() {
        List<Expense> list = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(URL)) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM expenses WHERE user_id=?");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Expense e = new Expense(
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        LocalDate.parse(rs.getString("date"))
                );
                e.setId(rs.getInt("id"));
                list.add(e);
            }
        } catch (Exception e) { }
        return list;
    }

    public static void addExpense(Expense e) {
        try (Connection c = DriverManager.getConnection(URL)) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO expenses(user_id,category,amount,date) VALUES(?,?,?,?)");
            ps.setInt(1, currentUserId);
            ps.setString(2, e.getCategory());
            ps.setDouble(3, e.getAmount());
            ps.setString(4, e.getDate().toString());
            ps.executeUpdate();
        } catch (Exception ex) { }
    }

    public static void deleteExpense(int id) {
        try (Connection c = DriverManager.getConnection(URL)) {
            PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM expenses WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { }
    }
}
