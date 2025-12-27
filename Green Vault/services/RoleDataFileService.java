package services;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for mapping user roles to their respective data files.
 * Each role has its own .txt file for storing data.
 */
public class RoleDataFileService {
    private static final Map<String, String> ROLE_FILE_MAP = new HashMap<>();
    
    static {
        // Map each role to its corresponding .txt file in data/ folder
        ROLE_FILE_MAP.put("Admin", "data/admin.txt");
        ROLE_FILE_MAP.put("Barangay Captain", "data/barangaycaptain.txt");
        ROLE_FILE_MAP.put("City Officer", "data/cityofficer.txt");
        ROLE_FILE_MAP.put("Garbage Collector", "data/garbagecollector.txt");
        ROLE_FILE_MAP.put("Barangay Member", "data/barangaymember.txt");
    }
    
    /**
     * Gets the data file name for a specific role.
     * @param role The user's role
     * @return The .txt file name for that role, or null if role not found
     */
    public static String getDataFile(String role) {
        return ROLE_FILE_MAP.get(role);
    }
    
    /**
     * Gets the full file path for a role's data file.
     * @param role The user's role
     * @return The full file path
     */
    public static String getDataFilePath(String role) {
        String fileName = getDataFile(role);
        if (fileName == null) {
            return "data/data_" + role.toLowerCase().replace(" ", "_") + ".txt";
        }
        return fileName;
    }
    
    /**
     * Checks if a role has a designated data file.
     * @param role The role to check
     * @return true if role has a file mapping, false otherwise
     */
    public static boolean hasDataFile(String role) {
        return ROLE_FILE_MAP.containsKey(role);
    }
}

