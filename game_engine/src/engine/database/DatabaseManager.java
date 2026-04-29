package engine.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager handles SQLite database connection for saving and loading player scores.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:game_data.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS high_scores (" +
                     " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     " player_name TEXT NOT NULL," +
                     " score INTEGER NOT NULL" +
                     ");";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public void saveScore(String playerName, int score) {
        String sql = "INSERT INTO high_scores(player_name, score) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    public void printHighScores() {
        String sql = "SELECT player_name, score FROM high_scores ORDER BY score DESC LIMIT 10";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("--- HIGH SCORES ---");
            while (rs.next()) {
                System.out.println(rs.getString("player_name") + "\t" + rs.getInt("score"));
            }
        } catch (SQLException e) {
            System.err.println("Error reading scores: " + e.getMessage());
        }
    }
}
