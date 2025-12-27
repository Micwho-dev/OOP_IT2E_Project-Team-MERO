package models;

/**
 * Represents a Barangay Staff user.
 */
public class BarangayStaff extends AbstractUser {
    public BarangayStaff(String username, String role, String barangay) { 
        super(username, role, barangay); 
    }
    
    @Override
    public String getPermissionsSummary() {
        return "Local Oversight and Community Engagement. Restricted to data within " + getBarangay() + ".";
    }
}

