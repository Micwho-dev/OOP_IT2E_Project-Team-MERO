package services;

import dao.UserDAO;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for user authentication operations.
 * Encapsulates user data and provides secure access methods.
 * Users are now stored in H2 database via UserDAO.
 */
public class UserAuthenticationService {

    /**
     * Authenticates a user by checking username and password.
     * @param username The username to check
     * @param password The password to verify
     * @return The user's role if authentication succeeds, null otherwise
     */
    public static String checkUserInDB(String username, String password) {
        try {
            return UserDAO.authenticateUser(username, password);
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets full user information (role, barangay).
     * @param username The username
     * @return Object array {role, barangay} or null if user not found
     */
    public static Object[] getUserInfo(String username) {
        try {
            return UserDAO.getUserInfo(username);
        } catch (SQLException e) {
            System.err.println("Error getting user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a new user in the system.
     * First saves to .txt file (based on role), then automatically imports to database.
     * @param username The username (must be unique)
     * @param password The user's password
     * @param role The user's role
     * @param barangay The user's barangay
     * @param id The user's ID (can be null for roles that don't require ID)
     * @throws IllegalStateException if username already exists
     */
    public static void registerUserInDB(String username, String password, String role, String barangay, String id) throws IllegalStateException {
        // Step 1: Save to .txt file first (based on role)
        String filePath = services.RoleDataFileService.getDataFilePath(role);
        File txtFile = new File(filePath);
        
        try {
            // Ensure data directory exists
            txtFile.getParentFile().mkdirs();
            
            // Determine if role requires ID (City Officer, Garbage Collector)
            // Note: Admin does NOT require ID format in file
            boolean hasId = id != null && !id.trim().isEmpty();
            boolean roleNeedsId = "City Officer".equals(role) || 
                                 "Garbage Collector".equals(role);
            
            // Append to .txt file (format depends on whether ID is present or role needs ID)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile, true))) {
                // Check if file is empty or doesn't exist, write header
                if (!txtFile.exists() || txtFile.length() == 0) {
                    writer.write("# User Accounts for " + role);
                    writer.newLine();
                    if (roleNeedsId) {
                        // For roles that need ID, always use format with ID column
                        writer.write("# Format: username|password|role|id|barangay");
                    } else {
                    writer.write("# Format: username|password|role|barangay");
                    }
                    writer.newLine();
                    writer.newLine();
                }
                
                // Write user data
                // For roles that need ID, always include ID column (even if empty)
                if (roleNeedsId) {
                    // Always use format with ID for roles that require ID
                    String idValue = hasId ? id : ""; // Use empty string if ID is null
                    writer.write(username + "|" + password + "|" + role + "|" + idValue + "|" + barangay);
                } else if (hasId) {
                    // For other roles, include ID only if present
                    writer.write(username + "|" + password + "|" + role + "|" + id + "|" + barangay);
                } else {
                    // No ID, use standard format
                writer.write(username + "|" + password + "|" + role + "|" + barangay);
                }
                writer.newLine();
                writer.flush();
            }
            
            // Step 2: Automatically import from .txt to database
            boolean success = UserDAO.createUser(username, password, role, barangay, id);
            if (!success) {
                throw new IllegalStateException("Username already exists.");
            }
        } catch (IOException e) {
            System.err.println("Error writing to .txt file: " + e.getMessage());
            e.printStackTrace();
            // Still try to save to database even if .txt write fails
            try {
                boolean success = UserDAO.createUser(username, password, role, barangay, id);
                if (!success) {
                    throw new IllegalStateException("Username already exists.");
                }
            } catch (SQLException sqlEx) {
                System.err.println("Error registering user: " + sqlEx.getMessage());
                sqlEx.printStackTrace();
                throw new IllegalStateException("Error registering user: " + sqlEx.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Error registering user: " + e.getMessage());
        }
    }
    
    /**
     * Registers a new user in the system (overloaded method without ID for backward compatibility).
     * @param username The username (must be unique)
     * @param password The user's password
     * @param role The user's role
     * @param barangay The user's barangay
     * @throws IllegalStateException if username already exists
     */
    public static void registerUserInDB(String username, String password, String role, String barangay) throws IllegalStateException {
        registerUserInDB(username, password, role, barangay, null);
    }
    
    /**
     * Deletes a user from the system.
     * Removes user from database, pending registrations, and their role-specific .txt file.
     * @param username The username to delete
     * @return true if successful, false if user not found
     */
    public static boolean deleteUserFromDB(String username) {
        try {
            // 1) Get user info first (to know which role file to update)
            Object[] userInfo = UserDAO.getUserInfo(username);
            String role = null;
            if (userInfo != null) {
                role = (String) userInfo[0]; // role is at index 0
            }
            
            // 2) Remove from pending_registration if they're still there
            UserApprovalService.removeFromPendingRegistration(username);
            
            // 3) Delete from database
            boolean dbDeleted = UserDAO.deleteUser(username);
            
            // 4) Remove from role-specific .txt file
            if (dbDeleted && role != null) {
                removeUserFromRoleFile(username, role);
            }
            
            return dbDeleted;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Removes a user from their role-specific .txt file.
     * @param username The username to remove
     * @param role The user's role (to determine which file to update)
     */
    private static void removeUserFromRoleFile(String username, String role) {
        String filePath = RoleDataFileService.getDataFilePath(role);
        File txtFile = new File(filePath);
        
        if (!txtFile.exists()) {
            return; // File doesn't exist, nothing to do
        }
        
        try {
            // Read all lines from file
            List<String> lines = new ArrayList<>();
            boolean inUserSection = false;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Detect user section
                    if (line.startsWith("# User Accounts")) {
                        inUserSection = true;
                        lines.add(line);
                        continue;
                    }
                    
                    // Detect waste records section (stop processing users here)
                    if (line.startsWith("# Waste Records")) {
                        inUserSection = false;
                        lines.add(line);
                        continue;
                    }
                    
                    // Skip user line if it matches the username to delete
                    if (inUserSection && !line.trim().isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 1 && parts[0].trim().equals(username)) {
                            // Skip this line (don't add it to lines list)
                            continue;
                        }
                    }
                    
                    lines.add(line);
                }
            }
            
            // Write updated content back to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            }
            
        } catch (IOException e) {
            System.err.println("Error removing user from role file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imports users from a .txt file (pipe-delimited format).
     * Format: username|password|role|barangay
     * Example: Rovick26|Qhmgsdtl26|Barangay Member|Central
     * 
     * @param file The .txt file to import from
     * @return Number of users successfully imported
     */
    public static int importUsersFromTxt(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        
        int imported = 0;
        int skipped = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse pipe-delimited format: username|password|role|barangay
                String[] parts = line.split("\\|");
                if (parts.length < 4) {
                    skipped++;
                    continue;
                }
                
                try {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    String barangay = parts[3].trim();
                    
                    // Create user in database
                    boolean success = UserDAO.createUser(username, password, role, barangay);
                    if (success) {
                        imported++;
                    } else {
                        skipped++; // Username might already exist
                    }
                } catch (SQLException e) {
                    System.err.println("Error importing line: " + line + " - " + e.getMessage());
                    skipped++;
                }
            }
            
            System.out.println("Import complete: " + imported + " users imported, " + skipped + " skipped");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return imported;
    }
}

