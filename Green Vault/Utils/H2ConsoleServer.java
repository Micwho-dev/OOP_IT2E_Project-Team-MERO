package utils;

import org.h2.tools.Server;
import java.sql.SQLException;

/**
 * Utility class to start H2 Console Server.
 * Provides a web-based interface to access and manage the H2 database.
 */
public class H2ConsoleServer {
    private static Server webServer;
    private static Server tcpServer;
    
    /**
     * Starts the H2 console web server.
     * Access the console at: http://localhost:8082
     */
    public static void startConsole() {
        try {
            // Ensure database folder exists
            DatabaseConfig.ensureDatabaseFolder();
            
            // Check if database exists, if not, initialize it
            java.io.File dbFile = new java.io.File(DatabaseConfig.getDatabaseFolder() + "/greenvault.mv.db");
            if (!dbFile.exists()) {
                System.out.println();
                System.out.println("========================================");
                System.out.println("WARNING: Database file not found!");
                System.out.println("========================================");
                System.out.println("Initializing database...");
                System.out.println();
                try {
                    DatabaseInitializer.initializeDatabase();
                    System.out.println();
                    System.out.println("Database initialized successfully!");
                } catch (Exception e) {
                    System.err.println("Failed to initialize database: " + e.getMessage());
                    e.printStackTrace();
                    throw new SQLException("Database initialization failed", e);
                }
            } else {
                System.out.println("Database file found: " + dbFile.getAbsolutePath());
            }
            
            // Start TCP server for remote connections
            // Get absolute path to current directory
            String baseDir = new java.io.File(".").getAbsolutePath();
            // Remove the trailing dot
            baseDir = baseDir.substring(0, baseDir.length() - 1);
            
            System.out.println();
            System.out.println("Starting TCP server...");
            System.out.println("Base directory: " + baseDir);
            
            tcpServer = Server.createTcpServer(
                "-tcp", 
                "-tcpAllowOthers", 
                "-tcpPort", "9092",
                "-baseDir", baseDir,
                "-tcpPassword", ""  // No password for TCP server
            ).start();
            
            System.out.println("TCP server started on port 9092");
            
            // Start web console server
            webServer = Server.createWebServer(
                "-web", "-webAllowOthers", "-webPort", String.valueOf(DatabaseConfig.CONSOLE_PORT)
            ).start();
            
            // Flush output to ensure it's visible immediately
            System.out.flush();
            System.err.flush();
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("H2 Console Server Started Successfully!");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Web Console: http://localhost:" + DatabaseConfig.CONSOLE_PORT);
            System.out.println();
            System.out.println("Connection Details:");
            System.out.println("  JDBC URL (Recommended): " + DatabaseConfig.getConsoleJdbcUrl());
            System.out.println("  JDBC URL (Alternative): " + DatabaseConfig.getConsoleJdbcUrlAbsolute());
            System.out.println("  Driver: " + DatabaseConfig.DB_DRIVER);
            System.out.println("  Username: " + DatabaseConfig.getUsername());
            System.out.println("  Password: " + DatabaseConfig.getPassword());
            System.out.println();
            System.out.println("Note: If the first JDBC URL doesn't work, try the alternative one.");
            System.out.println();
            System.out.println("========================================");
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("========================================");
            System.out.println();
            
            // Force flush to ensure output is visible
            System.out.flush();
            
        } catch (SQLException e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("Error starting H2 console server!");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("port") || errorMsg.contains("bind") || errorMsg.contains("already in use")) {
                System.err.println("PORT CONFLICT DETECTED!");
                System.err.println();
                System.err.println("Port 8082 (web console) or 9092 (TCP) is already in use.");
                System.err.println();
                System.err.println("Solutions:");
                System.err.println("1. Close any existing H2 server instances (Ctrl+C in their windows)");
                System.err.println("2. Find and kill the process using the port:");
                System.err.println("   - Open Command Prompt as Administrator");
                System.err.println("   - Run: netstat -ano | findstr :9092");
                System.err.println("   - Run: netstat -ano | findstr :8082");
                System.err.println("   - Note the PID and run: taskkill /PID <pid> /F");
                System.err.println("3. Restart your computer if ports are stuck");
            } else if (errorMsg.contains("jar") || errorMsg.contains("classpath") || errorMsg.contains("driver")) {
                System.err.println("H2 DRIVER ISSUE!");
                System.err.println();
                System.err.println("H2 JAR file might not be in classpath.");
                System.err.println("Make sure lib/h2-2.2.224.jar exists.");
            } else {
                System.err.println("Common issues:");
                System.err.println("1. Port 8082 or 9092 might already be in use");
                System.err.println("2. H2 JAR file might not be in classpath");
                System.err.println("3. Database file might be locked by another process");
                System.err.println("4. Insufficient permissions");
            }
            
            System.err.println();
            System.err.println("========================================");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("Unexpected error starting H2 console server!");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Stops the H2 console server.
     */
    public static void stopConsole() {
        if (webServer != null && webServer.isRunning(false)) {
            webServer.stop();
            System.out.println("Web console server stopped.");
        }
        if (tcpServer != null && tcpServer.isRunning(false)) {
            tcpServer.stop();
            System.out.println("TCP server stopped.");
        }
    }
    
    /**
     * Main method to start the console server standalone.
     */
    public static void main(String[] args) {
        // Add shutdown hook to stop servers gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down H2 console server...");
            stopConsole();
        }));
        
        startConsole();
        
        // Keep the server running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Server interrupted.");
        }
    }
}

