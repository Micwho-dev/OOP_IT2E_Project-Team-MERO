package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import services.UserAuthenticationService;
import services.UserApprovalService;
import services.RoleDataFileService;

/**
 * Registration frame for new user account creation.
 */
public class RegisterFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> barangayBox; 
    private JComboBox<String> roleBox;

    public RegisterFrame() {
        setTitle("GreenVault - Create Account");
        setSize(450, 500); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_GRAY);

        JPanel header = new JPanel();
        header.setBackground(UIConstants.PRIMARY_GREEN); 
        JLabel title = new JLabel("New User Registration");
        title.setForeground(Color.BLACK);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBorder(new EmptyBorder(30, 30, 30, 30));
        form.setBackground(UIConstants.BACKGROUND_GRAY);

        form.add(new JLabel("Username:"));
        userField = new JTextField();
        form.add(userField);

        form.add(new JLabel("Password:"));
        passField = new JPasswordField();
        form.add(passField);

        
        form.add(new JLabel("Barangay:"));
        List<String> barangayList = new ArrayList<>();
        barangayList.add("Central");
        barangayList.add("Matiao");
        barangayList.add("Sainz");
        barangayList.add("Don Martin Marundan");
        barangayList.add("Dahican");
        barangayList.add("Buso");
        Collections.sort(barangayList); 
        barangayList.add(0, "Select Barangay"); 
        
        barangayBox = new JComboBox<>(barangayList.toArray(new String[0]));
        form.add(barangayBox);

       


        form.add(new JLabel("Role:"));
        String[] roles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member"};
        roleBox = new JComboBox<>(roles);
        form.add(roleBox);

        // ID field for roles that need approval (hidden by default)
        JLabel idLabel = new JLabel("ID (Requires Approval):");
        JTextField idField = new JTextField(15);
        idLabel.setVisible(false);
        idField.setVisible(false);
        form.add(idLabel);
        form.add(idField);

        // Toggle visibility based on role selection
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            boolean isAdmin = "Admin".equals(selectedRole);
            // Admin does NOT need approval - register directly
            boolean needsIdApproval = !isAdmin && ("Garbage Collector".equals(selectedRole) ||
                                     "City Officer".equals(selectedRole));
            
            boolean isCityOfficer = "City Officer".equals(selectedRole);
            
            // Show/hide barangay (Admin doesn't need barangay, uses "System")
            boolean showBarangay = !needsIdApproval && !isAdmin;
            barangayBox.setVisible(showBarangay);
            barangayBox.setEnabled(showBarangay);
            
            // Show/hide ID field (only for roles that need approval)
            idLabel.setVisible(needsIdApproval);
            idField.setVisible(needsIdApproval);
            
            // Update ID label text
            if (isCityOfficer) {
                idLabel.setText("City Officer ID:");
            } else if (needsIdApproval) {
                idLabel.setText(selectedRole + " ID (Requires Approval):");
            }
            
            // Reset barangay selection for roles that need ID approval
            if (needsIdApproval) {
                barangayBox.setSelectedIndex(0);
            }
            
            form.revalidate();
            form.repaint();
        });

        JButton saveBtn = new JButton(" Register User");
        saveBtn.setBackground(UIConstants.ACCENT_GREEN);
        saveBtn.setForeground(Color.BLACK);
        
        JButton backBtn = new JButton("← Back to Login");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(UIConstants.BACKGROUND_GRAY);
        btnPanel.add(backBtn);
        btnPanel.add(saveBtn);


        saveBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String b = (String) barangayBox.getSelectedItem();
            String r = (String) roleBox.getSelectedItem();

            // Validation checks (GUI input validation)
            if(u.isEmpty() || p.isEmpty() || p.length() < 6) {
                JOptionPane.showMessageDialog(this, "Please ensure username and password (min 6 chars) are filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if role needs ID (only for roles requiring approval)
            boolean needsId = "Garbage Collector".equals(r) ||
                             "City Officer".equals(r);
            
            // Roles that need approval: City Officer, Garbage Collector
            boolean needsApproval = "City Officer".equals(r) || 
                                   "Garbage Collector".equals(r);
            
            // Handle Admin registration (no ID required, uses "System" as barangay)
            if ("Admin".equals(r)) {
                try {
                    String barangay = "System";
                    UserAuthenticationService.registerUserInDB(u, p, r, barangay);
                    JOptionPane.showMessageDialog(this, 
                        "Registration successful! Your " + r + " account has been created.\n" +
                        "Saved to: " + RoleDataFileService.getDataFilePath(r), 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    new LoginFrame().setVisible(true);
                    this.dispose();
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed: Uniqueness Violation", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            
            // Handle roles that need ID and approval
            if (needsId) {
                String roleId = idField.getText().trim();
                if (roleId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your " + r + " ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // If role needs approval, save to pending_registration.txt
                if (needsApproval) {
                    if (UserApprovalService.addPendingRegistration(u, p, r, roleId)) {
                        JOptionPane.showMessageDialog(this, 
                            "Registration submitted for approval!\n" +
                            "Your " + r + " account request has been sent to the admin for review.\n" +
                            "You will be notified once your account is approved.",
                            "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
                        new LoginFrame().setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error submitting registration for approval.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
            
            // Regular registration (with barangay)
            if (b.equals("Select Barangay")) {
                JOptionPane.showMessageDialog(this, "Please select a valid Barangay.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                UserAuthenticationService.registerUserInDB(u, p, r, b);
                JOptionPane.showMessageDialog(this, "Registration successful! Your account has been saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                new LoginFrame().setVisible(true);
                this.dispose();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed: Uniqueness Violation", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH); 
    }
}

