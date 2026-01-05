package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import services.UserAuthenticationService;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JTextField idField;
    private JCheckBox showPasswordCheckBox;

    public LoginFrame() {
        setTitle("GreenVault - Login");
        setSize(420, 500); // Slightly taller for better spacing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIConstants.ACCENT_GREEN);
        headerPanel.setPreferredSize(new Dimension(420, 80));
        headerPanel.setLayout(new GridBagLayout());
        
        JLabel titleLabel = new JLabel("GREENVAULT");
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerPanel.add(titleLabel);

        // --- Form Section ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        formPanel.setBackground(UIConstants.BACKGROUND_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Labels and Fields
        formPanel.add(createStyledLabel("Username"), gbc);
        userField = createStyledTextField();
        formPanel.add(userField, gbc);

        formPanel.add(createStyledLabel("Password"), gbc);
        passField = new JPasswordField();
        styleComponent(passField);
        formPanel.add(passField, gbc);

        // Show Password (Smaller, cleaner)
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBackground(UIConstants.BACKGROUND_GRAY);
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        showPasswordCheckBox.setFocusPainted(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('•');
            }
        });
        formPanel.add(showPasswordCheckBox, gbc);

        formPanel.add(createStyledLabel("ID (Staff Only)"), gbc);
        idField = createStyledTextField();
        idField.setToolTipText("Required for Captains, Officers, and Collectors");
        formPanel.add(idField, gbc);

        // --- Button Section ---
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setBorder(new EmptyBorder(10, 40, 40, 40));
        btnPanel.setBackground(UIConstants.BACKGROUND_GRAY);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn, UIConstants.PRIMARY_GREEN, Color.BLACK);

        JButton registerBtn = new JButton("Sign Up");
        styleButton(registerBtn, new Color(200, 200, 200), Color.BLACK);

        btnPanel.add(registerBtn);
        btnPanel.add(loginBtn);

        // --- Logic & Listeners ---
        loginBtn.addActionListener(e -> handleLogin());
        
        // Enter key to login
        KeyAdapter enterKeyHandler = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        };
        userField.addKeyListener(enterKeyHandler);
        passField.addKeyListener(enterKeyHandler);
        idField.addKeyListener(enterKeyHandler);

        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            this.dispose();
        });

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String id = idField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        String role = UserAuthenticationService.checkUserInDBWithID(user, pass, id.isEmpty() ? null : id);
        
        if (role != null) {
            if (isStaffRole(role) && id.isEmpty()) {
                showError("ID is required for " + role + " login.");
                return;
            }
            
            Object[] userInfo = UserAuthenticationService.getUserInfo(user);
            if (userInfo != null) {
                new DashboardFrame(user, role, (String) userInfo[1]).setVisible(true);
                this.dispose();
            }
        } else {
            handleLoginFailure(user, pass);
        }
    }

    private boolean isStaffRole(String role) {
        return "Barangay Captain".equals(role) || "City Officer".equals(role) || "Garbage Collector".equals(role);
    }

    private void handleLoginFailure(String user, String pass) {
        String tempRole = UserAuthenticationService.checkUserInDB(user, pass);
        if (isStaffRole(tempRole)) {
            showError("Invalid ID for " + tempRole + ".");
        } else {
            showError("Invalid Credentials.");
        }
    }

    // --- UI Helper Methods for Smoothness ---

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleComponent(field);
        return field;
    }

    private void styleComponent(JComponent comp) {
        comp.setPreferredSize(new Dimension(0, 35));
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Simple hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Update", JOptionPane.ERROR_MESSAGE);
    }
}
