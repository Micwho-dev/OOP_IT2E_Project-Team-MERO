package dao;

import utils.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Pending Registration operations.
 * Handles all database interactions for the pending_registrations table.
 */
public class PendingRegistrationDAO {
    
    /**
     * Adds a pending registration that requires approval.
     * @param username The username
     * @param password The password
     * @param role The role
     * @param id The ID that needs approval
     * @return true if successful, false if username already exists
     * @throws SQLException if database error occurs
     */
    public static boolean addPendingRegistration(String username, String password, String role, String id) throws SQLException {
        String sql = "INSERT INTO pending_registrations (username, password, role, id, status) VALUES (?, ?, ?, ?, 'Pending')";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, id);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            // Check if it's a duplicate key error
            if (e.getMessage().contains("PRIMARY KEY") || e.getMessage().contains("already exists")) {
                return false; // Username already exists
            }
            throw e; // Re-throw other SQL exceptions
        }
    }
    
    /**
     * Gets all pending registrations (status = 'Pending' and role != 'Admin').
     * @return List of pending registrations {username, password, role, id, status}
     * @throws SQLException if database error occurs
     */
    public static List<Object[]> getPendingRegistrations() throws SQLException {
        List<Object[]> pending = new ArrayList<>();
        String sql = "SELECT username, password, role, id, status FROM pending_registrations WHERE status = 'Pending' AND role != 'Admin'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                pending.add(new Object[]{
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("id"),
                    rs.getString("status")
                });
            }
        }
        
        return pending;
    }
    
    /**
     * Updates the status of a pending registration.
     * @param username The username
     * @param status The new status (Approved, Rejected, etc.)
     * @return true if successful, false if not found
     * @throws SQLException if database error occurs
     */
    public static boolean updateStatus(String username, String status) throws SQLException {
        String sql = "UPDATE pending_registrations SET status = ? WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, username);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }
    
    /**
     * Gets user data from a pending registration by username (only if status is 'Pending').
     * @param username The username
     * @return Object array {password, role, id} or null if not found
     * @throws SQLException if database error occurs
     */
    public static Object[] getPendingUserData(String username) throws SQLException {
        String sql = "SELECT password, role, id FROM pending_registrations WHERE username = ? AND status = 'Pending'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("id")
                    };
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets user data from pending registration regardless of status.
     * @param username The username
     * @return Object array {password, role, id} or null if not found
     * @throws SQLException if database error occurs
     */
    public static Object[] getUserDataByUsername(String username) throws SQLException {
        String sql = "SELECT password, role, id FROM pending_registrations WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("id")
                    };
                }
            }
        }
        
        return null;
    }
    
    /**
     * Deletes a pending registration.
     * @param username The username to delete
     * @return true if successful, false if not found
     * @throws SQLException if database error occurs
     */
    public static boolean delete(String username) throws SQLException {
        String sql = "DELETE FROM pending_registrations WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }
    
    /**
     * Checks if a pending registration exists.
     * @param username The username to check
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public static boolean exists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM pending_registrations WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        
        return false;
    }
}

