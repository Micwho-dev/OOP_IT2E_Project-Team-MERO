# DAO (Data Access Object) Layer

## Overview

The DAO layer provides a clean separation between business logic and database access. All database operations are handled through DAO classes, making the code more maintainable and testable.

## DAO Classes

### 1. UserDAO
Handles all database operations for the `users` table.

**Location**: `src/java/dao/UserDAO.java`

**Methods**:
- `createUser()` - Create a new user
- `authenticateUser()` - Authenticate user by username and password
- `getUserInfo()` - Get user information by username
- `getAllUsers()` - Get all users
- `getUsersByRole()` - Get users filtered by role
- `updateUser()` - Update user information
- `deleteUser()` - Delete a user
- `userExists()` - Check if user exists

**Example**:
```java
import java.dao.UserDAO;
import java.sql.SQLException;

// Create user
UserDAO.createUser("john", "password123", "Admin", "Central");

// Authenticate
String role = UserDAO.authenticateUser("john", "password123");

// Get user info
Object[] info = UserDAO.getUserInfo("john");
// Returns: {role, barangay}
```

### 2. WasteRecordDAO
Handles all database operations for the `waste_records` table.

**Location**: `src/java/dao/WasteRecordDAO.java`

**Methods**:
- `createWasteRecord()` - Create a new waste record
- `getAllWasteRecords()` - Get all waste records
- `getWasteRecordsByRole()` - Get waste records filtered by role
- `getWasteRecordById()` - Get waste record by ID
- `updateWasteRecord()` - Update waste record
- `deleteWasteRecord()` - Delete a waste record

**Example**:
```java
import java.dao.WasteRecordDAO;
import java.sql.SQLException;

// Create waste record
int id = WasteRecordDAO.createWasteRecord("Admin", "2025-01-01", "Central", 50.5, "Malata");

// Get all records
List<Object[]> records = WasteRecordDAO.getAllWasteRecords();

// Get by role
List<Object[]> adminRecords = WasteRecordDAO.getWasteRecordsByRole("Admin");
```

### 3. RequestDAO
Handles all database operations for the `requests` table.

**Location**: `src/java/dao/RequestDAO.java`

**Methods**:
- `createRequest()` - Create a new request
- `getAllRequests()` - Get all requests
- `getRequestsByBarangay()` - Get requests filtered by barangay
- `getRequestsByStatus()` - Get requests filtered by status
- `getRequestsByTargetRole()` - Get requests filtered by target role
- `getRequestById()` - Get request by ID
- `updateRequestStatus()` - Update request status
- `updateRequestTargetRole()` - Update request target role
- `deleteRequest()` - Delete a request

**Example**:
```java
import java.dao.RequestDAO;
import java.sql.SQLException;

// Create request
int id = RequestDAO.createRequest(
    "2025-01-01 10:00:00", "john", "Central", 
    "Waste Collection", "Location 1", "Description", 
    5, "Malata", "Pending", "Barangay Captain"
);

// Get by status
List<Object[]> pending = RequestDAO.getRequestsByStatus("Pending");

// Update status
RequestDAO.updateRequestStatus(id, "Approved");
```

## Package Structure

```
src/java/dao/
├── UserDAO.java          - User database operations
├── WasteRecordDAO.java   - Waste record database operations
├── RequestDAO.java      - Request database operations
└── README.md            - This file
```

## Usage Pattern

All DAO methods:
- Use `DatabaseConfig.getConnection()` for database connections
- Use try-with-resources for automatic resource management
- Throw `SQLException` for database errors
- Return appropriate data types (boolean, int, Object[], List<Object[]>)

## Benefits

1. **Separation of Concerns** - Database logic separated from business logic
2. **Reusability** - DAO methods can be used by multiple services
3. **Testability** - Easy to mock DAO classes for testing
4. **Maintainability** - Database changes only affect DAO layer
5. **Security** - All queries use PreparedStatement (prevents SQL injection)

## Migration from Service Classes

The existing Service classes (`UserAuthenticationService`, `WasteDataService`, `RequestService`) currently use file-based storage (.txt files). To migrate to database:

1. Update Service classes to use DAO methods instead of file operations
2. Keep Service classes for business logic
3. Use DAO classes for all database operations

## Example: Migrating UserAuthenticationService

**Before** (File-based):
```java
// UserAuthenticationService.java
public static String checkUserInDB(String username, String password) {
    // Read from .txt file
    Object[] data = users.get(username);
    // ...
}
```

**After** (Database-based):
```java
// UserAuthenticationService.java
public static String checkUserInDB(String username, String password) {
    try {
        return UserDAO.authenticateUser(username, password);
    } catch (SQLException e) {
        // Handle error
        return null;
    }
}
```

## Notes

- All DAO classes are in package `java.dao`
- All methods are static for easy access
- Connection management is handled automatically
- Use PreparedStatement for all queries (security best practice)

