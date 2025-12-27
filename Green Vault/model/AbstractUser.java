package models;

/**
 * Abstract base class for all user types in the GreenVault system.
 * Encapsulates common user properties and defines the permission summary interface.
 */
public abstract class AbstractUser {
    private final String username; // Made final for better immutability and encapsulation
    private final String role;
    private final String barangay; 

    public AbstractUser(String username, String role, String barangay) { 
        this.username = username;
        this.role = role;
        this.barangay = barangay;
    }

    /**
     * Returns a summary of user permissions specific to their role.
     * Must be implemented by each user subclass.
     */
    public abstract String getPermissionsSummary();

    // Getter methods for encapsulated fields
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public String getBarangay() { return barangay; }
}

