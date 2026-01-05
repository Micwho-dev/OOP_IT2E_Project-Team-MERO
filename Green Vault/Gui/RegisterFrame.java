package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import services.UserAuthenticationService;
import services.UserApprovalService;

/**
 * Registration frame for new user account creation.
 */
public class RegisterFrame extends JFrame {
    private JTextField userField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField surnameField;
    private JPasswordField passField;
    private JCheckBox showPasswordCheckBox;
    private JComboBox<String> barangayBox; 
    private JComboBox<String> roleBox;
    
    // Password regex pattern: at least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
    // NOTE: This validation ONLY applies to NEW user registrations. Existing users can still login
    // with their old passwords (no validation in LoginFrame).
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public RegisterFrame() {
        setTitle("GreenVault - Create Account");
        setSize(450, 700); 
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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(30, 30, 30, 30));
        form.setBackground(UIConstants.BACKGROUND_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username row
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        userField = new JTextField();
        form.add(userField, gbc);

        // First Name row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        firstNameField = new JTextField();
        form.add(firstNameField, gbc);
        
        // Middle Name row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Middle Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        middleNameField = new JTextField();
        form.add(middleNameField, gbc);
        
        // Surname row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Surname:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        surnameField = new JTextField();
        form.add(surnameField, gbc);
        
        // Password row
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passField = new JPasswordField();
        form.add(passField, gbc);
        
        // Show password checkbox row (spans both columns)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        showPasswordCheckBox = new JCheckBox("Show password");
        showPasswordCheckBox.setBackground(UIConstants.BACKGROUND_GRAY);
        showPasswordCheckBox.setFont(new Font("Arial", Font.PLAIN, 11));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passField.setEchoChar((char) 0); // Show password
            } else {
                passField.setEchoChar('•'); // Hide password
            }
        });
        form.add(showPasswordCheckBox, gbc);
        
        // Barangay row
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Barangay:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
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
        form.add(barangayBox, gbc);

        // Role row
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] roles = {"Admin", "Barangay Captain", "City Officer", "Garbage Collector", "Barangay Member"};
        roleBox = new JComboBox<>(roles);
        form.add(roleBox, gbc);

        // ID field for roles that need approval (hidden by default)
        JLabel idLabel = new JLabel("ID (Requires Approval):");
        JTextField idField = new JTextField(15);
        idLabel.setVisible(false);
        idField.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(idLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        form.add(idField, gbc);

        // Toggle visibility based on role selection
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            boolean isBarangayCaptain = "Barangay Captain".equals(selectedRole);
            // Roles that need Super Admin approval: Admin, City Officer, Garbage Collector, Barangay Captain
            boolean needsIdApproval = "Garbage Collector".equals(selectedRole) ||
                                     "City Officer".equals(selectedRole) ||
                                     "Admin".equals(selectedRole) ||
                                     "Barangay Captain".equals(selectedRole);
            // All roles that need approval also need ID
            boolean needsId = needsIdApproval;
            
            boolean isCityOfficer = "City Officer".equals(selectedRole);
            
            // Show/hide barangay
            // Barangay Captain needs to select barangay (even though they need approval)
            // Admin doesn't need barangay, uses "System"
            // City Officer and Garbage Collector don't need barangay selection
            boolean showBarangay = !needsIdApproval || isBarangayCaptain;
            barangayBox.setVisible(showBarangay);
            barangayBox.setEnabled(showBarangay);
            
            // Show/hide ID field (for roles that need ID: City Officer, Garbage Collector, Barangay Captain)
            idLabel.setVisible(needsId);
            idField.setVisible(needsId);
            
            // Update ID label text
            if (isCityOfficer) {
                idLabel.setText("City Officer ID:");
            } else if (isBarangayCaptain) {
                idLabel.setText("Barangay Captain ID:");
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
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String surname = surnameField.getText().trim();
            String p = new String(passField.getPassword());
            String b = (String) barangayBox.getSelectedItem();
            String r = (String) roleBox.getSelectedItem();
            
            // Normalize empty strings to null for optional fields (middleName)
            if (middleName.isEmpty()) {
                middleName = null;
            }

            // Validation checks (GUI input validation)
            if(u.isEmpty() || firstName.isEmpty() || surname.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please ensure username, first name, surname, and password are filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Password regex validation (ONLY for new registrations - existing users not affected)
            if (!pattern.matcher(p).matches()) {
                JOptionPane.showMessageDialog(this, 
                    "Password must meet the following requirements:\n" +
                    "• At least 8 characters long\n" +
                    "• At least 1 uppercase letter (A-Z)\n" +
                    "• At least 1 lowercase letter (a-z)\n" +
                    "• At least 1 number (0-9)\n" +
                    "• At least 1 special character (@$!%*?&)",
                    "Password Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if role needs ID (City Officer, Garbage Collector, Barangay Captain)
            boolean needsId = "Garbage Collector".equals(r) ||
                             "City Officer".equals(r) ||
                             "Barangay Captain".equals(r);
            
            // Roles that need Super Admin approval: Admin, City Officer, Garbage Collector, Barangay Captain
            boolean needsApproval = "City Officer".equals(r) || 
                                   "Garbage Collector".equals(r) ||
                                   "Admin".equals(r) ||
                                   "Barangay Captain".equals(r);
            
            // Handle Admin registration (needs Super Admin approval, ID can be entered but is optional)
            if ("Admin".equals(r)) {
                // Admin can optionally enter their ID during registration
                // If not entered, Super Admin will assign it during approval
                String adminId = idField.getText().trim();
                
                // Admin registration goes through pending approval (Super Admin must approve)
                // Pass name fields to pending registration
                if (UserApprovalService.addPendingRegistration(u, p, r, adminId, null, firstName, middleName, surname)) {
                    JOptionPane.showMessageDialog(this, 
                        "Registration submitted for approval!\n" +
                        "Your Admin account request has been sent to the Super Admin for review.\n" +
                        "You will be notified once your account is approved.",
                        "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
                    new LoginFrame().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error submitting registration for approval.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            
            // Handle roles that need ID and approval (City Officer, Garbage Collector, Barangay Captain)
            if (needsId && needsApproval) {
                String roleId = idField.getText().trim();
                if (roleId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your " + r + " ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // For Barangay Captain, validate barangay selection
                String selectedBarangay = null;
                if ("Barangay Captain".equals(r)) {
                    if (b == null || b.equals("Select Barangay")) {
                        JOptionPane.showMessageDialog(this, "Please select a Barangay.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    selectedBarangay = b;
                }
                
                // All roles that need approval go through pending registration
                // Pass barangay for Barangay Captain, null for others
                // Pass name fields to pending registration
                if (UserApprovalService.addPendingRegistration(u, p, r, roleId, selectedBarangay, firstName, middleName, surname)) {
                        JOptionPane.showMessageDialog(this, 
                            "Registration submitted for approval!\n" +
                        "Your " + r + " account request has been sent to the Super Admin for review.\n" +
                            "You will be notified once your account is approved.",
                            "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
                        new LoginFrame().setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error submitting registration for approval.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            
            // Regular registration (with barangay, no ID)
            if (b.equals("Select Barangay")) {
                JOptionPane.showMessageDialog(this, "Please select a valid Barangay.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                UserAuthenticationService.registerUserInDB(u, p, r, b, null, firstName, middleName, surname);
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

