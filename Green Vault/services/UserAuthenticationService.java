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
     * Authenticates a user by checking username, password, and ID.
     * ID is required for Barangay Captain, optional for other roles.
     * @param username The username to check
     * @param password The password to verify
     * @param id The user's ID (required for Barangay Captain)
     * @return The user's role if authentication succeeds, null otherwise
     */
    public static String checkUserInDBWithID(String username, String password, String id) {
        try {
            return UserDAO.authenticateUserWithID(username, password, id);
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets user ID by username.
     * @param username The username
     * @return The user's ID or null if not found or no ID set
     */
    public static String getUserID(String username) {
        try {
            return UserDAO.getUserID(username);
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
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
    public static void registerUserInDB(String username, String password, String role, String barangay, String id, String firstName, String middleName, String surname) throws IllegalStateException {
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
            boolean success = UserDAO.createUser(username, password, role, barangay, id, firstName, middleName, surname);
            if (!success) {
                throw new IllegalStateException("Username already exists.");
            }
        } catch (IOException e) {
            System.err.println("Error writing to .txt file: " + e.getMessage());
            e.printStackTrace();
            // Still try to save to database even if .txt write fails
            try {
                boolean success = UserDAO.createUser(username, password, role, barangay, id, firstName, middleName, surname);
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
        registerUserInDB(username, password, role, barangay, null, null, null, null);
    }
    
    public static void registerUserInDB(String username, String password, String role, String barangay, String id) throws IllegalStateException {
        registerUserInDB(username, password, role, barangay, id, null, null, null);
    }
    
    /**
     * Deletes a user from the system.
     * Removes user from database, pending registrations, and their role-specific .txt file.
     * @param username The username to delete
     * @return true if successful, false if user not found
     */
    public static boolean deleteUserFromDB(String username) {
        boolean dbDeleted = false;
        boolean fileDeleted = false;
        String role = null;
        
        try {
            System.out.println("=== Starting deletion for user: " + username + " ===");
            
            // 1) Get user info first (to know which role file to update) - MUST DO BEFORE DELETING FROM DB
            Object[] userInfo = UserDAO.getUserInfo(username);
            if (userInfo != null) {
                role = (String) userInfo[0]; // role is at index 0
                System.out.println("User role retrieved: " + role);
            } else {
                System.out.println("WARNING: User info not found in database for: " + username);
            }
            
            // 2) Remove from pending_registration if they're still there
            UserApprovalService.removeFromPendingRegistration(username);
            
            // 3) Delete from database
            dbDeleted = UserDAO.deleteUser(username);
            System.out.println("Database deletion result: " + dbDeleted);
            
            // 4) ALWAYS try to remove from .txt file (even if DB deletion failed, user might still be in file)
            // First try the specific role file, then search all files to be sure
            if (role != null) {
                // Try to remove from the specific role file
                System.out.println("Attempting to remove from file for role: " + role);
                fileDeleted = removeUserFromRoleFile(username, role);
            }
            
            // ALWAYS search ALL role files to ensure complete deletion (handles duplicates, wrong role, etc.)
            System.out.println("Searching ALL role files to ensure complete deletion...");
            String[] allRoles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member", "Waste Manager"};
            for (String r : allRoles) {
                boolean deleted = removeUserFromRoleFile(username, r);
                if (deleted) {
                    fileDeleted = true;
                }
            }
            
            if (dbDeleted && fileDeleted) {
                System.out.println("✓ User successfully deleted from BOTH database and file.");
            } else if (dbDeleted && !fileDeleted) {
                System.out.println("⚠ User deleted from database but NOT found in file (may not exist in file).");
            } else if (!dbDeleted && fileDeleted) {
                System.out.println("⚠ User deleted from file but NOT from database (may not exist in database).");
            } else {
                System.out.println("✗ User deletion failed from both database and file.");
            }
            
            System.out.println("=== Deletion process completed for: " + username + " ===");
            return dbDeleted; // Return DB deletion result (primary indicator)
            
        } catch (SQLException e) {
            System.err.println("ERROR deleting user: " + e.getMessage());
            e.printStackTrace();
            
            // Even if DB deletion failed, still try to remove from file
            if (role != null) {
                removeUserFromRoleFile(username, role);
            } else {
                String[] allRoles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member", "Waste Manager"};
                for (String r : allRoles) {
                    removeUserFromRoleFile(username, r);
                }
            }
            
            return false;
        }
    }
    
    /**
     * Removes a user from their role-specific .txt file.
     * @param username The username to remove
     * @param role The user's role (to determine which file to update)
     */
    private static boolean removeUserFromRoleFile(String username, String role) {
        String filePath = RoleDataFileService.getDataFilePath(role);
        File txtFile = new File(filePath);
        
        System.out.println("Attempting to remove user '" + username + "' from file: " + filePath);
        System.out.println("File exists: " + txtFile.exists());
        System.out.println("Absolute path: " + txtFile.getAbsolutePath());
        
        if (!txtFile.exists()) {
            System.out.println("File does not exist: " + filePath + " (user may not be in this file)");
            return false; // File doesn't exist, nothing to do
        }
        
        try {
            // Read all lines from file
            List<String> lines = new ArrayList<>();
            boolean inUserSection = false;
            boolean userFound = false;
            int lineNumber = 0;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
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
                    // Handle both formats: with ID (username|password|role|id|barangay) 
                    // and without ID (username|password|role|barangay)
                    if (inUserSection && !line.trim().isEmpty() && !line.startsWith("#")) {
                        String trimmedLine = line.trim();
                        String[] parts = trimmedLine.split("\\|");
                        if (parts.length >= 1) {
                            String lineUsername = parts[0].trim();
                            // Match username exactly (case-sensitive)
                            if (lineUsername.equals(username.trim())) {
                                // Skip this line (don't add it to lines list) - removes ALL occurrences
                                userFound = true;
                                System.out.println("✓ Found and removing user '" + username + "' at line " + lineNumber + " from " + filePath);
                                System.out.println("  Line content: " + trimmedLine);
                                continue; // Skip this line, don't add to lines list
                            }
                        }
                    }
                    
                    lines.add(line);
                }
            }
            
            if (!userFound) {
                System.out.println("User '" + username + "' not found in file: " + filePath + " (may not exist in this file)");
                return false; // User not in this file
            } else {
                System.out.println("✓ User '" + username + "' found and will be removed from file.");
            }
            
            // Write updated content back to file ONLY if user was found
            if (userFound) {
                // Use FileWriter with append=false to overwrite the entire file
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(txtFile, false));
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.flush();
                    System.out.println("✓ Successfully updated file: " + filePath);
                    System.out.println("  Total lines written: " + lines.size());
                    System.out.println("  File path: " + txtFile.getAbsolutePath());
                    return true; // Successfully removed from file
                } catch (IOException writeEx) {
                    System.err.println("ERROR writing to file " + filePath + ": " + writeEx.getMessage());
                    writeEx.printStackTrace();
                    return false;
                } finally {
                    // Always close the writer
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException closeEx) {
                            System.err.println("Error closing file writer: " + closeEx.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("User '" + username + "' not found in file: " + filePath);
                return false; // User not found in this file
            }
            
        } catch (IOException e) {
            System.err.println("ERROR removing user from role file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
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
     * Authenticates a user by checking username, password, and ID.
     * ID is required for Barangay Captain, optional for other roles.
     * @param username The username to check
     * @param password The password to verify
     * @param id The user's ID (required for Barangay Captain)
     * @return The user's role if authentication succeeds, null otherwise
     */
    public static String checkUserInDBWithID(String username, String password, String id) {
        try {
            return UserDAO.authenticateUserWithID(username, password, id);
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets user ID by username.
     * @param username The username
     * @return The user's ID or null if not found or no ID set
     */
    public static String getUserID(String username) {
        try {
            return UserDAO.getUserID(username);
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
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
    public static void registerUserInDB(String username, String password, String role, String barangay, String id, String firstName, String middleName, String surname) throws IllegalStateException {
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
            boolean success = UserDAO.createUser(username, password, role, barangay, id, firstName, middleName, surname);
            if (!success) {
                throw new IllegalStateException("Username already exists.");
            }
        } catch (IOException e) {
            System.err.println("Error writing to .txt file: " + e.getMessage());
            e.printStackTrace();
            // Still try to save to database even if .txt write fails
            try {
                boolean success = UserDAO.createUser(username, password, role, barangay, id, firstName, middleName, surname);
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
        registerUserInDB(username, password, role, barangay, null, null, null, null);
    }
    
    public static void registerUserInDB(String username, String password, String role, String barangay, String id) throws IllegalStateException {
        registerUserInDB(username, password, role, barangay, id, null, null, null);
    }
    
    /**
     * Deletes a user from the system.
     * Removes user from database, pending registrations, and their role-specific .txt file.
     * @param username The username to delete
     * @return true if successful, false if user not found
     */
    public static boolean deleteUserFromDB(String username) {
        boolean dbDeleted = false;
        boolean fileDeleted = false;
        String role = null;
        
        try {
            System.out.println("=== Starting deletion for user: " + username + " ===");
            
            // 1) Get user info first (to know which role file to update) - MUST DO BEFORE DELETING FROM DB
            Object[] userInfo = UserDAO.getUserInfo(username);
            if (userInfo != null) {
                role = (String) userInfo[0]; // role is at index 0
                System.out.println("User role retrieved: " + role);
            } else {
                System.out.println("WARNING: User info not found in database for: " + username);
            }
            
            // 2) Remove from pending_registration if they're still there
            UserApprovalService.removeFromPendingRegistration(username);
            
            // 3) Delete from database
            dbDeleted = UserDAO.deleteUser(username);
            System.out.println("Database deletion result: " + dbDeleted);
            
            // 4) ALWAYS try to remove from .txt file (even if DB deletion failed, user might still be in file)
            // First try the specific role file, then search all files to be sure
            if (role != null) {
                // Try to remove from the specific role file
                System.out.println("Attempting to remove from file for role: " + role);
                fileDeleted = removeUserFromRoleFile(username, role);
            }
            
            // ALWAYS search ALL role files to ensure complete deletion (handles duplicates, wrong role, etc.)
            System.out.println("Searching ALL role files to ensure complete deletion...");
            String[] allRoles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member", "Waste Manager"};
            for (String r : allRoles) {
                boolean deleted = removeUserFromRoleFile(username, r);
                if (deleted) {
                    fileDeleted = true;
                }
            }
            
            if (dbDeleted && fileDeleted) {
                System.out.println("✓ User successfully deleted from BOTH database and file.");
            } else if (dbDeleted && !fileDeleted) {
                System.out.println("⚠ User deleted from database but NOT found in file (may not exist in file).");
            } else if (!dbDeleted && fileDeleted) {
                System.out.println("⚠ User deleted from file but NOT from database (may not exist in database).");
            } else {
                System.out.println("✗ User deletion failed from both database and file.");
            }
            
            System.out.println("=== Deletion process completed for: " + username + " ===");
            return dbDeleted; // Return DB deletion result (primary indicator)
            
        } catch (SQLException e) {
            System.err.println("ERROR deleting user: " + e.getMessage());
            e.printStackTrace();
            
            // Even if DB deletion failed, still try to remove from file
            if (role != null) {
                removeUserFromRoleFile(username, role);
            } else {
                String[] allRoles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member", "Waste Manager"};
                for (String r : allRoles) {
                    removeUserFromRoleFile(username, r);
                }
            }
            
            return false;
        }
    }
    
    /**
     * Removes a user from their role-specific .txt file.
     * @param username The username to remove
     * @param role The user's role (to determine which file to update)
     */
    private static boolean removeUserFromRoleFile(String username, String role) {
        String filePath = RoleDataFileService.getDataFilePath(role);
        File txtFile = new File(filePath);
        
        System.out.println("Attempting to remove user '" + username + "' from file: " + filePath);
        System.out.println("File exists: " + txtFile.exists());
        System.out.println("Absolute path: " + txtFile.getAbsolutePath());
        
        if (!txtFile.exists()) {
            System.out.println("File does not exist: " + filePath + " (user may not be in this file)");
            return false; // File doesn't exist, nothing to do
        }
        
        try {
            // Read all lines from file
            List<String> lines = new ArrayList<>();
            boolean inUserSection = false;
            boolean userFound = false;
            int lineNumber = 0;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
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
                    // Handle both formats: with ID (username|password|role|id|barangay) 
                    // and without ID (username|password|role|barangay)
                    if (inUserSection && !line.trim().isEmpty() && !line.startsWith("#")) {
                        String trimmedLine = line.trim();
                        String[] parts = trimmedLine.split("\\|");
                        if (parts.length >= 1) {
                            String lineUsername = parts[0].trim();
                            // Match username exactly (case-sensitive)
                            if (lineUsername.equals(username.trim())) {
                                // Skip this line (don't add it to lines list) - removes ALL occurrences
                                userFound = true;
                                System.out.println("✓ Found and removing user '" + username + "' at line " + lineNumber + " from " + filePath);
                                System.out.println("  Line content: " + trimmedLine);
                                continue; // Skip this line, don't add to lines list
                            }
                        }
                    }
                    
                    lines.add(line);
                }
            }
            
            if (!userFound) {
                System.out.println("User '" + username + "' not found in file: " + filePath + " (may not exist in this file)");
                return false; // User not in this file
            } else {
                System.out.println("✓ User '" + username + "' found and will be removed from file.");
            }
            
            // Write updated content back to file ONLY if user was found
            if (userFound) {
                // Use FileWriter with append=false to overwrite the entire file
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(txtFile, false));
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.flush();
                    System.out.println("✓ Successfully updated file: " + filePath);
                    System.out.println("  Total lines written: " + lines.size());
                    System.out.println("  File path: " + txtFile.getAbsolutePath());
                    return true; // Successfully removed from file
                } catch (IOException writeEx) {
                    System.err.println("ERROR writing to file " + filePath + ": " + writeEx.getMessage());
                    writeEx.printStackTrace();
                    return false;
                } finally {
                    // Always close the writer
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException closeEx) {
                            System.err.println("Error closing file writer: " + closeEx.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("User '" + username + "' not found in file: " + filePath);
                return false; // User not found in this file
            }
            
        } catch (IOException e) {
            System.err.println("ERROR removing user from role file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
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

