package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

import services.RequestService;
import services.WasteDataService;
import services.UserAuthenticationService;
import services.UserApprovalService;
import dao.UserDAO;
import java.sql.SQLException;

/**
 * Separate window for viewing high-level reports and exporting data to CSV/text files.
 * This helps satisfy the requirement for a dedicated reports/results window.
 */
public class ReportsFrame extends JFrame {

    public ReportsFrame(String username, String role, String barangay) {
        setTitle("GreenVault - Reports & Exports");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UIConstants.BACKGROUND_GRAY);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        header.setBackground(UIConstants.PRIMARY_GREEN);

        JLabel title = new JLabel("Reports & Data Export");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("View simple summaries and export Requests / Waste Records to CSV or text files.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(Color.DARK_GRAY);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Requests Summary", createRequestsTab());
        tabs.addTab("Waste Records Summary", createWasteTab());
        tabs.addTab("Users", createUsersTab());
        tabs.addTab("Pending Registrations", createPendingRegistrationsTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_GRAY);

        String[] columns = {"ID", "Timestamp", "Requester", "Barangay", "Type", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Lightweight summary: load from RequestService helper that already uses DAO
        for (Object[] row : RequestService.getAllRequestsSummary()) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setBackground(UIConstants.BACKGROUND_GRAY);

        JButton importBtn = new JButton("📥 Import from .txt");
        importBtn.setBackground(new Color(70, 130, 180));
        importBtn.setForeground(Color.WHITE);
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import Requests from .txt file");
            chooser.setCurrentDirectory(new File("data"));
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                int imported = RequestService.importRequestsFromTxt(file, "Barangay Captain");
                JOptionPane.showMessageDialog(panel, 
                    imported + " requests imported from:\n" + file.getAbsolutePath(),
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                // Refresh table
                model.setRowCount(0);
                for (Object[] row : RequestService.getAllRequestsSummary()) {
                    model.addRow(row);
                }
            }
        });

        JButton exportCsvBtn = new JButton("📄 Export to CSV");
        exportCsvBtn.setBackground(UIConstants.ACCENT_GREEN);
        exportCsvBtn.setForeground(Color.BLACK);
        exportCsvBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Requests as CSV");
            int result = chooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                boolean ok = RequestService.exportRequestsToCsv(file);
                if (ok) {
                    JOptionPane.showMessageDialog(panel, "Requests exported to:\n" + file.getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Error exporting requests. Please check console for details.",
                            "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottom.add(importBtn);
        bottom.add(exportCsvBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWasteTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_GRAY);

        String[] columns = {"ID", "Role", "Date", "Area/Location", "Weight (kg)", "Type"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load all records directly for reporting
        for (Object[] row : WasteDataService.getAllWasteRecordsForReport()) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setBackground(UIConstants.BACKGROUND_GRAY);

        JButton importBtn = new JButton("📥 Import from .txt");
        importBtn.setBackground(new Color(70, 130, 180));
        importBtn.setForeground(Color.WHITE);
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import Waste Records from .txt file");
            chooser.setCurrentDirectory(new File("data"));
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                int imported = WasteDataService.importWasteRecordsFromTxt(file, "Garbage Collector");
                JOptionPane.showMessageDialog(panel, 
                    imported + " waste records imported from:\n" + file.getAbsolutePath(),
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                // Refresh table
                model.setRowCount(0);
                for (Object[] row : WasteDataService.getAllWasteRecordsForReport()) {
                    model.addRow(row);
                }
            }
        });

        JButton exportCsvBtn = new JButton("📄 Export to CSV");
        exportCsvBtn.setBackground(UIConstants.ACCENT_GREEN);
        exportCsvBtn.setForeground(Color.BLACK);
        exportCsvBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Waste Records as CSV");
            int result = chooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                boolean ok = WasteDataService.exportWasteRecordsToCsv(file);
                if (ok) {
                    JOptionPane.showMessageDialog(panel, "Waste records exported to:\n" + file.getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Error exporting waste records. Please check console for details.",
                            "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottom.add(importBtn);
        bottom.add(exportCsvBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_GRAY);

        String[] columns = {"Username", "Role", "Barangay"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load users from database
        try {
            java.util.List<Object[]> users = UserDAO.getAllUsers();
            for (Object[] user : users) {
                // Format: {username, password, role, barangay}
                model.addRow(new Object[]{user[0], user[2], user[3]});
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setBackground(UIConstants.BACKGROUND_GRAY);

        JButton importBtn = new JButton("📥 Import from .txt");
        importBtn.setBackground(new Color(70, 130, 180));
        importBtn.setForeground(Color.WHITE);
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import Users from .txt file");
            chooser.setCurrentDirectory(new File("data"));
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                int imported = UserAuthenticationService.importUsersFromTxt(file);
                JOptionPane.showMessageDialog(panel, 
                    imported + " users imported from:\n" + file.getAbsolutePath(),
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                // Refresh table
                model.setRowCount(0);
                try {
                    java.util.List<Object[]> users = UserDAO.getAllUsers();
                    for (Object[] user : users) {
                        model.addRow(new Object[]{user[0], user[2], user[3]});
                    }
                } catch (SQLException ex) {
                    System.err.println("Error refreshing users: " + ex.getMessage());
                }
            }
        });

        bottom.add(importBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPendingRegistrationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIConstants.BACKGROUND_GRAY);

        String[] columns = {"Username", "Role", "ID", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load pending registrations from database
        java.util.List<Object[]> pending = UserApprovalService.getPendingRegistrations();
        for (Object[] reg : pending) {
            // Format: {username, password, role, id, status}
            model.addRow(new Object[]{reg[0], reg[2], reg[3], reg[4]});
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setBackground(UIConstants.BACKGROUND_GRAY);

        JButton importBtn = new JButton("📥 Import from .txt");
        importBtn.setBackground(new Color(70, 130, 180));
        importBtn.setForeground(Color.WHITE);
        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Import Pending Registrations from .txt file");
            chooser.setCurrentDirectory(new File("data"));
            int result = chooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                int imported = UserApprovalService.importPendingRegistrationsFromTxt(file);
                JOptionPane.showMessageDialog(panel, 
                    imported + " pending registrations imported from:\n" + file.getAbsolutePath(),
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                // Refresh table
                model.setRowCount(0);
                java.util.List<Object[]> pendingList = UserApprovalService.getPendingRegistrations();
                for (Object[] reg : pendingList) {
                    model.addRow(new Object[]{reg[0], reg[2], reg[3], reg[4]});
                }
            }
        });

        bottom.add(importBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }
}


