package com.mycompany.resumeanalyse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginGUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, signupButton;

    public LoginGUI() {
        setTitle("Login - Resume Analyzer");
        setSize(500, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 248, 255));

        // Title
        JLabel titleLabel = new JLabel("Resume Analyzer Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        centerPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        centerPanel.add(passwordField, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240, 248, 255));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(new LoginAction());
        bottomPanel.add(loginButton);

        // Signup button
        signupButton = new JButton("Create Account");
        signupButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        signupButton.setBackground(new Color(0, 153, 76));
        signupButton.setForeground(Color.WHITE);
        signupButton.addActionListener(e -> openSignupDialog());
        bottomPanel.add(signupButton);

        add(bottomPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
    }

    // Login validation
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (validateLogin(username, password)) {
                JOptionPane.showMessageDialog(LoginGUI.this, "Login Successful!");
                new ResumeGUI().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginGUI.this, 
                    "Invalid username or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Signup popup window
    private void openSignupDialog() {
        JDialog dialog = new JDialog(this, "Create New Account", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField newUserField = new JTextField(15);
        JPasswordField newPassField = new JPasswordField(15);
        JPasswordField confirmPassField = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(newUserField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(confirmPassField, gbc);

        JButton createBtn = new JButton("Create Account");
        createBtn.setBackground(new Color(0, 153, 76));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 1; gbc.gridy = 3;
        dialog.add(createBtn, gbc);

        createBtn.addActionListener(e -> {
            String user = newUserField.getText();
            String pass = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields.");
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.");
                return;
            }

            if (addUserToDB(user, pass)) {
                JOptionPane.showMessageDialog(dialog, "Account created successfully!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username already exists.");
            }
        });

        dialog.setVisible(true);
    }

    // Insert new user
    private boolean addUserToDB(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String check = "SELECT * FROM app_users WHERE username=?";
            PreparedStatement ps1 = conn.prepareStatement(check);
            ps1.setString(1, username);
            ResultSet rs = ps1.executeQuery();
            if (rs.next()) return false;

            String insert = "INSERT INTO app_users(username, password) VALUES(?, ?)";
            PreparedStatement ps2 = conn.prepareStatement(insert);
            ps2.setString(1, username);
            ps2.setString(2, password);

            return ps2.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Validate login
    private boolean validateLogin(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM app_users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
